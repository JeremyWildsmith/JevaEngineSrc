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

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;

public final class TraverseRouteTask extends MovementTask
{

	private Route m_travelRoute;

	private Vector2F m_routeDestination;

	private float m_fRadius;
	
	private Entity m_entity;
	
	private WorldDirection[] m_allowedMovements;
	
	public TraverseRouteTask(@Nullable Vector2F destination, WorldDirection[] allowedMovements, float speed, float fRadius)
	{
		super(speed);
		m_travelRoute = new Route();
		m_allowedMovements = allowedMovements;
		m_routeDestination = destination;
		m_fRadius = fRadius;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.MovementTask#begin(jeva.world.Entity)
	 */
	@Override
	public final void begin(Entity entity)
	{
		super.begin(entity);
		
		m_entity = entity;
		
		try
		{
			World world = entity.getWorld();

			if (m_routeDestination == null)
				m_travelRoute = Route.createRandom(new WorldNavigationRoutingRules(m_allowedMovements), m_entity.getWorld(), getSpeed(), m_entity.getLocation().round(), (int) m_fRadius, true);
			else
				m_travelRoute = Route.create(new WorldNavigationRoutingRules(m_allowedMovements), world, getSpeed(), m_entity.getLocation().round(), m_routeDestination.round(), m_fRadius, true);
		} catch (IncompleteRouteException r)
		{
			m_travelRoute = new Route(getSpeed(), r.getIncompleteRoute());
			
			//Destination no longer valid, if unable to reach it.
			m_routeDestination = null;
		}
		
		if(m_routeDestination == null)
			m_routeDestination = new Vector2F(m_travelRoute.peek(m_travelRoute.length() - 1).getLocation());
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.MovementTask#getDestination()
	 */
	@Override
	protected final Vector2F getDestination()
	{
		if (m_travelRoute.getCurrentTarget() == null)
			return m_routeDestination;
		else if(m_travelRoute.getCurrentTarget().getLocation().equals(m_routeDestination.round()))
		{
			m_travelRoute.nextTarget();
			return m_routeDestination;
		}
		return new Vector2F(m_travelRoute.getCurrentTarget().getLocation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.MovementTask#atDestination()
	 */
	@Override
	protected final boolean atDestination()
	{
		if (m_travelRoute.getCurrentTarget() != null)
			m_travelRoute.getCurrentTarget().unschedule(m_travelRoute);

		return !(m_travelRoute.nextTarget() || !m_routeDestination.equals(m_entity.getLocation()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.MovementTask#blocking()
	 */
	@Override
	protected boolean blocking()
	{
		this.cancel();
		return false;
	}

	public final void truncate(int maxSteps)
	{
		m_travelRoute.truncate(maxSteps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.MovementTask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_travelRoute.unschedule();

		super.cancel();
	}
}
