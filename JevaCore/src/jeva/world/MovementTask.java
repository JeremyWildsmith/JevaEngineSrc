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
package jeva.world;

import jeva.math.Vector2F;

import com.sun.istack.internal.Nullable;


public abstract class MovementTask implements ITask
{
	/** The m_traveler. */
	private ITraveler m_traveler;

	/** The m_query cancel. */
	private boolean m_queryCancel;

	/** The m_last destination. */
	private Vector2F m_lastDestination = null;

	
	public MovementTask(ITraveler traveler)
	{
		m_queryCancel = false;

		m_traveler = traveler;
	}

	
	protected ITraveler getTraveler()
	{
		return m_traveler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{
		m_queryCancel = false;
		m_lastDestination = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#end()
	 */
	public void end() { }

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	public final boolean doCycle(int deltaTime)
	{
		if (m_queryCancel)
		{
			m_queryCancel = false;
			m_traveler.updateMovement(new Vector2F());
			return true;
		}

		if (m_lastDestination == null || !m_lastDestination.equals(getDestination()))
		{
			m_lastDestination = new Vector2F(getDestination());
			m_traveler.setDestination(m_lastDestination);
		}

		Vector2F deltaMovement = getDestination().difference(m_traveler.getLocation());
		Vector2F advancement = (deltaMovement.isZero() ? new Vector2F() : deltaMovement.normalize().multiply(m_traveler.getSpeed() * ((float) deltaTime / 1000)));

		deltaMovement = (deltaMovement.getLengthSquared() < advancement.getLengthSquared() ? deltaMovement : advancement);

		RouteNode lastNode = m_traveler.getWorld().getRouteNode(m_traveler.getLocation().round());
		RouteNode nextNode = m_traveler.getWorld().getRouteNode(m_traveler.getLocation().add(deltaMovement).round());

		// If we're about to move on to target node.
		if (lastNode != nextNode)
		{
			// Try to take control of it
			if (nextNode.isTraversable() && nextNode.take())
			{
				lastNode.release();
			} else
			{
				m_traveler.updateMovement(new Vector2F());

				blocking();

				return m_queryCancel;
			}
		}// If we've just arrived at our target
		else if (deltaMovement.isZero())
		{
			if (atDestination())
			{
				m_traveler.updateMovement(deltaMovement);
				nextNode.release();
				return true;
			} else
				return false;
		}

		m_traveler.updateMovement(deltaMovement);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_traveler.getWorld().getRouteNode(m_traveler.getLocation().round()).release();
		m_queryCancel = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#isParallel()
	 */
	@Override
	public final boolean isParallel()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#ignoresPause()
	 */
	@Override
	public final boolean ignoresPause()
	{
		return false;
	}

	
	protected abstract void blocking();

	
	@Nullable
	protected abstract Vector2F getDestination();

	
	protected abstract boolean atDestination();

	
	protected abstract boolean hasNext();

	
	public interface ITraveler
	{
		
		void updateMovement(Vector2F delta);

		
		void setDestination(Vector2F target);

		
		World getWorld();

		
		Vector2F getLocation();

		
		float getSpeed();
	}
}
