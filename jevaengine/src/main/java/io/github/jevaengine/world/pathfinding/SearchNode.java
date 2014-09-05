package io.github.jevaengine.world.pathfinding;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.World;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public final class SearchNode
{
	private static final int DIAGONAL_COST = 7;
	private static final int HORIZONTAL_VERTICAL_COST = 5;

	private SearchNode m_parent;

	private ArrayList<SearchNode> m_children;

	private Direction m_direction;

	private Vector2D m_location;

	private World m_world;

	public SearchNode(World world, SearchNode parent, Direction direction, Vector2D location)
	{
		m_parent = parent;
		m_children = new ArrayList<SearchNode>();
		m_location = location;
		m_direction = direction;
		m_world = world;
	}
	
	public SearchNode[] getChildren()
	{
		return m_children.toArray(new SearchNode[m_children.size()]);
	}

	public Direction getDirection()
	{
		return m_direction;
	}

	protected ArrayList<Vector2F> traverseRoute()
	{
		ArrayList<Vector2F> traverseRoute = (m_parent == null ? new ArrayList<Vector2F>() : m_parent.traverseRoute());

		traverseRoute.add(new Vector2F(m_location));

		return traverseRoute;
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
	protected SearchNode getChildNode(Direction direction)
	{
		for (SearchNode node : m_children)
		{
			if (node.m_direction == direction)
				return node;
		}

		return null;
	}

	public SearchNode addNode(Direction direction)
	{
		if (getChildNode(direction) != null)
			return getChildNode(direction);

		Vector2D location = m_location.add(direction.getDirectionVector());

		SearchNode step = new SearchNode(m_world, this, direction, location);

		//Attempt to determine if it is more efficient to get to this node via our parent (or any of it's parents)
		//before attempting this route.
		if (m_parent == null)
			m_children.add(step);
		else
		{
			Vector2D parentRelative = location.difference(m_parent.getLocation());
			Direction dirFromParent = Direction.fromVector(new Vector2F(location.difference(m_parent.getLocation())));
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

	public void removeNode(Direction direction)
	{
		SearchNode node = getChildNode(direction);

		if (node != null)
		{
			m_children.remove(node);
		}
	}

	public int getCostToReachNode()
	{
		int cost = (m_parent == null ? 0 : m_parent.getCostToReachNode());

		return cost + getMovementCost();
	}

	public int getCostOfNodeToGoal(Vector2D target)
	{
		return ((Math.abs(target.x - m_location.x) + Math.abs(target.y - m_location.y))) * HORIZONTAL_VERTICAL_COST;
	}

	public int getCost(Vector2D target)
	{
		return getCostOfNodeToGoal(target) + getCostToReachNode();
	}

	public Vector2D getLocation()
	{
		return m_location;
	}

	public Vector2D getLocation(Direction dir)
	{
		return m_location.add(dir.getDirectionVector());
	}

	public boolean isIneffective(Direction dir)
	{
		Vector2D resultant = getLocation().add(dir.getDirectionVector());

		for (Vector2F node : traverseRoute())
		{
			if (node.round().equals(resultant))
				return true;
		}

		return false;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + m_location.hashCode();
		return result;
	}

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
		if (!m_location.equals(other.m_location))
			return false;
		return true;
	}
}