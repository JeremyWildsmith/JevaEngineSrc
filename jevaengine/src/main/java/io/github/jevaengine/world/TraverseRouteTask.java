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

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;

public class TraverseRouteTask extends MovementTask
{

	private Route m_travelRoute;

	private Vector2D m_routeDestination;

	private float m_fRadius;

	public TraverseRouteTask(IRouteTraveler traveler, @Nullable Vector2D destination, float fRadius)
	{
		super(traveler);

		m_travelRoute = new Route();

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

		try
		{
			World world = entity.getWorld();

			if (m_routeDestination == null)
			{
				m_travelRoute = Route.createRandom(new WorldNavigationRoutingRules((IRouteTraveler) getTraveler()), world, getTraveler().getSpeed(), getTraveler().getLocation().round(), (int) m_fRadius, true);
			} else
			{
				m_travelRoute = Route.create(new WorldNavigationRoutingRules((IRouteTraveler) getTraveler()), world, getTraveler().getSpeed(), getTraveler().getLocation().round(), m_routeDestination, m_fRadius, true);
			}
		} catch (IncompleteRouteException r)
		{
			m_travelRoute = new Route(getTraveler().getSpeed(), r.getIncompleteRoute());
		}
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
			return getTraveler().getLocation();

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

		return !m_travelRoute.nextTarget();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.MovementTask#blocking()
	 */
	@Override
	protected final void blocking()
	{
		this.cancel();
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

	public interface IRouteTraveler extends ITraveler
	{

		public WorldDirection[] getAllowedMovements();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.MovementTask#hasNext()
	 */
	@Override
	protected boolean hasNext()
	{
		return m_travelRoute.hasNext();
	}
}
