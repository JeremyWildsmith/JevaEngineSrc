/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;

public class Route
{

	private static final int MAX_PATH_ITERATIONS = 40;

	private static final float PATHING_CONSISTANCY_PROBABILITY = 0.7F;

	private ArrayList<RouteNode> m_path;

	private float m_fSpeed;

	protected Route()
	{
		m_path = new ArrayList<RouteNode>();
		m_fSpeed = 0.0F;
	}

	protected Route(float fSpeed, RouteNode... path)
	{
		m_path = new ArrayList<RouteNode>(Arrays.asList(path));
		m_fSpeed = fSpeed;
	}

	public static Route create(IRoutingRules routingRules, World world, float fTraverseSpeed, Vector2D startPoint, Vector2D endPoint, float fDestinationRadius, boolean isScheduled) throws IncompleteRouteException
	{
		if (startPoint.equals(endPoint))
			return new Route(fTraverseSpeed, new RouteNode[]
			{ world.getRouteNode(endPoint) });

		ArrayList<SearchNode> open = new ArrayList<SearchNode>();
		ArrayList<SearchNode> closed = new ArrayList<SearchNode>();

		SearchNode base = new SearchNode(world, null, WorldDirection.Zero, world.getRouteNode(startPoint));
		open.add(base);

		SearchNode best = null;

		for (int iterations = 0; iterations < MAX_PATH_ITERATIONS && !open.isEmpty(); iterations++)
		{
			best = open.get(0);
			
			for (SearchNode node : open)
			{
				if (node.getCost(endPoint) < best.getCost(endPoint))
					best = node;
			}

			if (best.getLocation().equals(endPoint))
			{
				ArrayList<RouteNode> route = best.traverseRoute();
				route.remove(0);
				return new Route(fTraverseSpeed, route.toArray(new RouteNode[route.size()]));
			} else
			{
				open.remove(best);
				closed.add(best);

				for (WorldDirection dir : routingRules.getMovements(world, best, endPoint))
				{
					SearchNode step = best.addNode(dir);

					if (!open.contains(step) && !closed.contains(step))
						open.add(step);
				}
			}
		}

		if (best != null)
		{
			ArrayList<RouteNode> route = best.traverseRoute();
			route.remove(0);
			throw new IncompleteRouteException(route.toArray(new RouteNode[route.size()]));
		} else
			throw new IncompleteRouteException(new RouteNode[] {});

	}

	public static Route createRandom(IRoutingRules routingRules, World world, float fTraverseSpeed, Vector2D startPoint, int length, boolean isScheduled)
	{
		SearchNode tail = new SearchNode(world, null, WorldDirection.Zero, world.getRouteNode(startPoint));
		SearchNode head = tail;

		Random random = new Random();

		ArrayList<RouteNode> routeNodes;

		try
		{
			WorldDirection lastDirection = WorldDirection.Zero;

			while (head.getRoute().length < length)
			{
				for (WorldDirection dir : routingRules.getMovements(world, head, null))
					head.addNode(dir);

				if (head.getChildren().length <= 0)
					break;
				else
				{
					SearchNode consistantNode = lastDirection == WorldDirection.Zero ? null : head.getChildNode(lastDirection);

					if (consistantNode == null || random.nextFloat() > PATHING_CONSISTANCY_PROBABILITY)
						head = head.getChildren()[random.nextInt(head.getChildren().length)];
					else
						head = consistantNode;

					lastDirection = head.getDirection();
				}
			}

			routeNodes = new ArrayList<RouteNode>(Arrays.asList(head.getRoute()));
		} catch (IncompleteRouteException e)
		{
			routeNodes = new ArrayList<RouteNode>(Arrays.asList(e.getIncompleteRoute()));
		}

		Route route = new Route(fTraverseSpeed, routeNodes.subList(1, routeNodes.size()).toArray(new RouteNode[routeNodes.size() - 1]));

		if (isScheduled)
			route.schedule();

		return route;

	}

	public void schedule()
	{
		for (RouteNode node : m_path)
		{
			node.schedule(this);
		}
	}

	public void unschedule()
	{
		for (RouteNode node : m_path)
		{
			node.unschedule(this);
		}
	}

	public void truncate(int maxSteps)
	{
		if (m_path.size() <= maxSteps)
			return;

		for (int i = maxSteps; i <= m_path.size(); i++)
			m_path.get(i).unschedule(this);

		m_path = new ArrayList<RouteNode>(m_path.subList(0, maxSteps));
	}

	public int getArrivalTime()
	{
		return (int) Math.round((1 / m_fSpeed) * length());
	}

	public float getAverageTraverseSpeed()
	{
		return m_fSpeed;
	}

	public int length()
	{
		return m_path.size();
	}

	public RouteNode getCurrentTarget()
	{
		if (m_path.isEmpty())
			return null;

		return m_path.get(0);
	}

	public boolean nextTarget()
	{
		if (!m_path.isEmpty())
			m_path.remove(0);

		return !m_path.isEmpty();
	}

	public boolean hasNext()
	{
		return m_path.size() > 1;
	}
	
	public RouteNode peek(int ahead)
	{
		return m_path.get(ahead);
	}

	public void addTarget(RouteNode node)
	{
		node.schedule(this);
		m_path.add(node);
	}

	public void clear()
	{
		for (RouteNode node : m_path)
			node.unschedule(this);

		m_path.clear();
	}

	public static class SearchNode
	{

		private static final int DIAGONAL_COST = 7;

		private static final int HORIZONTAL_VERTICAL_COST = 5;

		private SearchNode m_parent;

		private ArrayList<SearchNode> m_children;

		private WorldDirection m_direction;

		private RouteNode m_routeNode;

		private World m_world;

		public SearchNode(World world, SearchNode parent, WorldDirection direction, RouteNode routeNode)
		{
			m_parent = parent;
			m_children = new ArrayList<Route.SearchNode>();
			m_routeNode = routeNode;
			m_direction = direction;
			m_world = world;
		}

		public SearchNode[] getChildren()
		{
			return m_children.toArray(new SearchNode[m_children.size()]);
		}

		public WorldDirection getDirection()
		{
			return m_direction;
		}

		public RouteNode[] getRoute()
		{
			ArrayList<RouteNode> route = traverseRoute();

			return route.toArray(new RouteNode[route.size()]);
		}

		protected ArrayList<RouteNode> traverseRoute()
		{
			ArrayList<RouteNode> route = (m_parent == null ? new ArrayList<RouteNode>() : m_parent.traverseRoute());

			route.add(m_routeNode);

			return route;
		}

		protected int getMovementCost()
		{
			switch (m_direction)
			{
				case XMinus:
				case XPlus:
				case YMinus:
				case YPlus:
					return HORIZONTAL_VERTICAL_COST;
				case XYMinus:
				case XYPlus:
				case XYPlusMinus:
				case XYMinusPlus:
					return DIAGONAL_COST;
				case Zero:
					return 0;
			}

			throw new NoSuchElementException();
		}

		@Nullable
		protected SearchNode getChildNode(WorldDirection direction)
		{
			for (SearchNode node : m_children)
			{
				if (node.m_direction == direction)
					return node;
			}

			return null;
		}

		public SearchNode addNode(WorldDirection direction)
		{
			if (getChildNode(direction) != null)
				return getChildNode(direction);

			Vector2D location = m_routeNode.getLocation().add(direction.getDirectionVector());

			SearchNode step = new SearchNode(m_world, this, direction, m_world.getRouteNode(location));

			if (m_parent == null)
				m_children.add(step);
			else
			{
				Vector2D parentRelative = location.difference(m_parent.getLocation());
				WorldDirection dirFromParent = WorldDirection.fromVector(new Vector2F(location.difference(m_parent.getLocation())));
				SearchNode parentNode = m_parent.getChildNode(dirFromParent);

				if (parentRelative.getLength() > 1 || parentNode == null)
					m_children.add(step);
				else
				{
					if (parentNode.getMovementCost() > step.getMovementCost())
					{
						m_parent.removeNode(dirFromParent);
						step = addNode(direction);
					} else
						step = parentNode;
				}
			}

			return step;
		}

		public void removeNode(WorldDirection direction)
		{
			SearchNode node = getChildNode(direction);

			if (node != null)
			{
				m_children.remove(node);
			}
		}

		public int getCostToReachNode()
		{
			int bost = (m_parent == null ? 0 : m_parent.getCostToReachNode());

			// TODO Reimplement cost based on node traffic
			// ArrayList<RouteNode> route = traverseRoute();

			bost += getMovementCost();// * (1.0F + 2 *
										// m_routeNode.getNodeTrafficGrade(new
										// Route(0.01F, route.toArray(new
										// RouteNode[route.size()]))));

			return bost;
		}

		public int getCostOfNodeToGoal(Vector2D target)
		{
			return ((Math.abs(target.x - m_routeNode.getLocation().x) + Math.abs(target.y - m_routeNode.getLocation().y))) * HORIZONTAL_VERTICAL_COST;
		}

		public int getCost(Vector2D target)
		{
			return getCostOfNodeToGoal(target) + getCostToReachNode();
		}

		public Vector2D getLocation()
		{
			return m_routeNode.getLocation();
		}

		public Vector2D getLocation(WorldDirection dir)
		{
			return m_routeNode.getLocation().add(dir.getDirectionVector());
		}

		public boolean isIneffective(WorldDirection dir)
		{
			Vector2D resultant = getLocation().add(dir.getDirectionVector());

			for (RouteNode node : traverseRoute())
			{
				if (node.getLocation().equals(resultant))
					return true;
			}

			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_routeNode == null) ? 0 : m_routeNode.hashCode());
			return result;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			else if (obj == null)
				return false;
			else if (!(obj instanceof SearchNode))
				return false;

			SearchNode other = (SearchNode) obj;
			if (m_routeNode == null)
			{
				if (other.m_routeNode != null)
					return false;
			} else if (!m_routeNode.equals(other.m_routeNode))
				return false;
			return true;
		}

	}
}
