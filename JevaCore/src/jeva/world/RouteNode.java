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
package jeva.world;

import java.util.ArrayList;

import jeva.math.Vector2D;


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

	
	public RouteNode(World parent, Vector2D location)
	{
		m_owned = false;
		m_location = location;

		m_schedules = new ArrayList<Route>();

		m_parent = parent;
	}

	
	public boolean isTraversable()
	{
		return m_parent.getTileEffects(m_location).isTraversable;
	}

	
	public boolean take()
	{
		if (m_owned)
			return false;

		m_owned = true;

		return true;
	}

	
	public void release()
	{
		m_owned = false;
	}

	
	public void schedule(Route route)
	{
		m_schedules.add(route);
	}

	
	public void unschedule(Route route)
	{
		if (m_schedules.contains(route))
		{
			m_schedules.remove(route);
		}
	}

	
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

	
	public Vector2D getLocation()
	{
		return m_location;
	}
}
