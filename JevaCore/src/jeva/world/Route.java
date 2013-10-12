/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Random;

import com.sun.istack.internal.Nullable;

import jeva.math.Vector2D;
import jeva.math.Vector2F;

/**
 * The Class Route.
 * 
 * @author Scott
 */
public class Route
{

	/** The Constant MAX_PATH_ITERATIONS. */
	private static final int MAX_PATH_ITERATIONS = 40;

	/** The Constant PATHING_CONSISTANCY_PROBABILITY. */
	private static final float PATHING_CONSISTANCY_PROBABILITY = 0.7F;

	/** The m_path. */
	private ArrayList<RouteNode> m_path;

	/** The m_f speed. */
	private float m_fSpeed;

	/**
	 * Instantiates a new route.
	 */
	protected Route()
	{
		m_path = new ArrayList<RouteNode>();
		m_fSpeed = 0.0F;
	}

	/**
	 * Instantiates a new route.
	 * 
	 * @param fSpeed
	 *            the f speed
	 * @param path
	 *            the path
	 */
	protected Route(float fSpeed, RouteNode... path)
	{
		m_path = new ArrayList<RouteNode>(Arrays.asList(path));
		m_fSpeed = fSpeed;
	}

	/**
	 * Creates the.
	 * 
	 * @param routingRules
	 *            the routing rules
	 * @param world
	 *            the world
	 * @param fTraverseSpeed
	 *            the f traverse speed
	 * @param startPoint
	 *            the start point
	 * @param endPoint
	 *            the end point
	 * @param fDestinationRadius
	 *            the f destination radius
	 * @param isScheduled
	 *            the is scheduled
	 * @return the route
	 * @throws IncompleteRouteException
	 *             the incomplete route exception
	 */
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
			best = null;
			for (SearchNode node : open)
			{
				if (best == null || node.getCost(endPoint) < best.getCost(endPoint))
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

				for (WorldDirection dir : routingRules.getMovements(best, endPoint))
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

	/**
	 * Creates the random.
	 * 
	 * @param routingRules
	 *            the routing rules
	 * @param world
	 *            the world
	 * @param fTraverseSpeed
	 *            the f traverse speed
	 * @param startPoint
	 *            the start point
	 * @param length
	 *            the length
	 * @param isScheduled
	 *            the is scheduled
	 * @return the route
	 */
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
				for (WorldDirection dir : routingRules.getMovements(head, null))
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

	/**
	 * Schedule.
	 */
	public void schedule()
	{
		for (RouteNode node : m_path)
		{
			node.schedule(this);
		}
	}

	/**
	 * Unschedule.
	 */
	public void unschedule()
	{
		for (RouteNode node : m_path)
		{
			node.unschedule(this);
		}
	}

	/**
	 * Truncate.
	 * 
	 * @param maxSteps
	 *            the max steps
	 */
	public void truncate(int maxSteps)
	{
		if (m_path.size() <= maxSteps)
			return;

		for (int i = maxSteps; i <= m_path.size(); i++)
			m_path.get(i).unschedule(this);

		m_path = new ArrayList<RouteNode>(m_path.subList(0, maxSteps));
	}

	/**
	 * Gets the arrival time.
	 * 
	 * @return the arrival time
	 */
	public int getArrivalTime()
	{
		return (int) Math.round((1 / m_fSpeed) * getRouteLength());
	}

	/**
	 * Gets the average traverse speed.
	 * 
	 * @return the average traverse speed
	 */
	public float getAverageTraverseSpeed()
	{
		return m_fSpeed;
	}

	/**
	 * Gets the route length.
	 * 
	 * @return the route length
	 */
	public float getRouteLength()
	{
		return m_path.size();
	}

	/**
	 * Gets the current target.
	 * 
	 * @return the current target
	 */
	public RouteNode getCurrentTarget()
	{
		if (m_path.isEmpty())
			return null;

		return m_path.get(0);
	}

	/**
	 * Next target.
	 * 
	 * @return true, if successful
	 */
	public boolean nextTarget()
	{
		if (!m_path.isEmpty())
			m_path.remove(0);

		if (m_path.isEmpty())
		{
			return false;
		}

		return true;
	}

	/**
	 * Checks for next.
	 * 
	 * @return true, if successful
	 */
	public boolean hasNext()
	{
		return m_path.size() > 1;
	}

	/**
	 * Adds the target.
	 * 
	 * @param node
	 *            the node
	 */
	public void addTarget(RouteNode node)
	{
		node.schedule(this);
		m_path.add(node);
	}

	/**
	 * Clear.
	 */
	public void clear()
	{
		for (RouteNode node : m_path)
			node.unschedule(this);

		m_path.clear();
	}

	/**
	 * The Class SearchNode.
	 */
	public static class SearchNode
	{

		/** The Constant DIAGONAL_COST. */
		private static final int DIAGONAL_COST = 7;

		/** The Constant HORIZONTAL_VERTICAL_COST. */
		private static final int HORIZONTAL_VERTICAL_COST = 5;

		/** The m_parent. */
		private SearchNode m_parent;

		/** The m_children. */
		private ArrayList<SearchNode> m_children;

		/** The m_direction. */
		private WorldDirection m_direction;

		/** The m_route node. */
		private RouteNode m_routeNode;

		/** The m_world. */
		private World m_world;

		/**
		 * Instantiates a new search node.
		 * 
		 * @param world
		 *            the world
		 * @param parent
		 *            the parent
		 * @param direction
		 *            the direction
		 * @param routeNode
		 *            the route node
		 */
		public SearchNode(World world, SearchNode parent, WorldDirection direction, RouteNode routeNode)
		{
			m_parent = parent;
			m_children = new ArrayList<Route.SearchNode>();
			m_routeNode = routeNode;
			m_direction = direction;
			m_world = world;
		}

		/**
		 * Gets the children.
		 * 
		 * @return the children
		 */
		public SearchNode[] getChildren()
		{
			return m_children.toArray(new SearchNode[m_children.size()]);
		}

		/**
		 * Gets the direction.
		 * 
		 * @return the direction
		 */
		public WorldDirection getDirection()
		{
			return m_direction;
		}

		/**
		 * Gets the route.
		 * 
		 * @return the route
		 */
		public RouteNode[] getRoute()
		{
			ArrayList<RouteNode> route = traverseRoute();

			return route.toArray(new RouteNode[route.size()]);
		}

		/**
		 * Traverse route.
		 * 
		 * @return the array list
		 */
		protected ArrayList<RouteNode> traverseRoute()
		{
			ArrayList<RouteNode> route = (m_parent == null ? new ArrayList<RouteNode>() : m_parent.traverseRoute());

			route.add(m_routeNode);

			return route;
		}

		/**
		 * Gets the movement cost.
		 * 
		 * @return the movement cost
		 */
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

		/**
		 * Gets the child node.
		 * 
		 * @param direction
		 *            the direction
		 * @return the child node
		 */
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

		/**
		 * Adds the node.
		 * 
		 * @param direction
		 *            the direction
		 * @return the search node
		 */
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

		/**
		 * Removes the node.
		 * 
		 * @param direction
		 *            the direction
		 */
		public void removeNode(WorldDirection direction)
		{
			SearchNode node = getChildNode(direction);

			if (node != null)
			{
				m_children.remove(node);
			}
		}

		/**
		 * Gets the cost to reach node.
		 * 
		 * @return the cost to reach node
		 */
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

		/**
		 * Gets the cost of node to goal.
		 * 
		 * @param target
		 *            the target
		 * @return the cost of node to goal
		 */
		public int getCostOfNodeToGoal(Vector2D target)
		{
			return ((Math.abs(target.x - m_routeNode.getLocation().x) + Math.abs(target.y - m_routeNode.getLocation().y))) * HORIZONTAL_VERTICAL_COST;
		}

		/**
		 * Gets the cost.
		 * 
		 * @param target
		 *            the target
		 * @return the cost
		 */
		public int getCost(Vector2D target)
		{
			return getCostOfNodeToGoal(target) + getCostToReachNode();
		}

		/**
		 * Gets the location.
		 * 
		 * @return the location
		 */
		public Vector2D getLocation()
		{
			return m_routeNode.getLocation();
		}

		/**
		 * Gets the location.
		 * 
		 * @param dir
		 *            the dir
		 * @return the location
		 */
		public Vector2D getLocation(WorldDirection dir)
		{
			return m_routeNode.getLocation().add(dir.getDirectionVector());
		}

		/**
		 * Checks if is ineffective.
		 * 
		 * @param dir
		 *            the dir
		 * @return true, if is ineffective
		 */
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
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
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
