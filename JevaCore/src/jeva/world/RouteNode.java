/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

import java.util.ArrayList;

import jeva.math.Vector2D;

/**
 * The Class RouteNode.
 * 
 * @author Scott
 */
public class RouteNode
{

	/** The m_schedules. */
	private ArrayList<Route> m_schedules;

	/** The m_owned. */
	private boolean m_owned;

	/** The m_location. */
	private Vector2D m_location;

	/** The m_parent. */
	private World m_parent;

	/**
	 * Instantiates a new route node.
	 * 
	 * @param parent
	 *            the parent
	 * @param location
	 *            the location
	 */
	public RouteNode(World parent, Vector2D location)
	{
		m_owned = false;
		m_location = location;

		m_schedules = new ArrayList<Route>();

		m_parent = parent;
	}

	/**
	 * Checks if is traversable.
	 * 
	 * @return true, if is traversable
	 */
	public boolean isTraversable()
	{
		return m_parent.getTileEffects(m_location).isTraversable;
	}

	/**
	 * Take.
	 * 
	 * @return true, if successful
	 */
	public boolean take()
	{
		if (m_owned)
			return false;

		m_owned = true;

		return true;
	}

	/**
	 * Release.
	 */
	public void release()
	{
		m_owned = false;
	}

	/**
	 * Schedule.
	 * 
	 * @param route
	 *            the route
	 */
	public void schedule(Route route)
	{
		m_schedules.add(route);
	}

	/**
	 * Unschedule.
	 * 
	 * @param route
	 *            the route
	 */
	public void unschedule(Route route)
	{
		if (m_schedules.contains(route))
		{
			m_schedules.remove(route);
		}
	}

	/**
	 * Gets the node traffic grade.
	 * 
	 * @param takenRoute
	 *            the taken route
	 * @return the node traffic grade
	 */
	public float getNodeTrafficGrade(Route takenRoute)
	{
		/*
		 * float conflictGrade = 0;
		 * 
		 * for (Route route : m_schedules) { float arrivalDelta =
		 * Math.abs(route.getArrivalTime() - takenRoute.getArrivalTime());
		 * 
		 * float firstOccupyTime = (route.getArrivalTime() >
		 * takenRoute.getArrivalTime() ? 1 /
		 * takenRoute.getAverageTraverseSpeed() : 1 /
		 * route.getAverageTraverseSpeed());
		 * 
		 * if (arrivalDelta > firstOccupyTime) { conflictGrade +=
		 * (firstOccupyTime / arrivalDelta) * 1.5F; } }
		 * 
		 * if (conflictGrade == 0) { return 0; } else if (conflictGrade < 0.9F)
		 * { return .2F; } else if (conflictGrade < 1.5F) { return .4F; } else
		 * if (conflictGrade < 2.8F) { return 0.7F; } else { return 1.0F; }
		 */

		return 0.0F;
	}

	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public Vector2D getLocation()
	{
		return m_location;
	}
}
