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
package io.github.jevaengine.world;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;

public abstract class MovementTask implements ITask
{
	private boolean m_queryCancel;

	private Vector2F m_lastDestination = null;

	private Observers m_observers = new Observers();
	
	private Entity m_entity;
	
	private float m_speed;
	
	public MovementTask(float speed)
	{
		m_queryCancel = false;
		m_speed = speed;
	}
	
	public void addObserver(IMovementObserver observer)
	{
		m_observers.add(observer);
	}
	
	public void removeObserver(IMovementObserver observer)
	{
		m_observers.remove(observer);
	}

	protected float getSpeed()
	{
		return m_speed;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{
		m_entity = entity;
		m_queryCancel = false;
		m_lastDestination = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#end()
	 */
	@Override
	public void end() { }

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	@Override
	public final boolean doCycle(int deltaTime)
	{
		if (m_queryCancel)
		{
			m_observers.updateMovement(new Vector2F());
			return true;
		}

		if (m_lastDestination == null || !m_lastDestination.equals(getDestination()))
		{
			m_lastDestination = new Vector2F(getDestination());
			m_observers.movingTowards(m_lastDestination);
		}

		Vector2F deltaMovement = getDestination().difference(m_entity.getLocation());
		Vector2F advancement = (deltaMovement.isZero() ? new Vector2F() : deltaMovement.normalize().multiply(m_speed * ((float) deltaTime / 1000)));

		deltaMovement = (deltaMovement.getLengthSquared() < advancement.getLengthSquared() ? deltaMovement : advancement);

		RouteNode lastNode = m_entity.getWorld().getRouteNode(m_entity.getLocation().round());
		RouteNode nextNode = m_entity.getWorld().getRouteNode(m_entity.getLocation().add(deltaMovement).round());

		// If we're about to move on to target node.
		if (lastNode != nextNode)
		{
			boolean isTraversable = nextNode.isTraversable() && nextNode.take();
			
			if(!isTraversable)
				isTraversable |= blocking();
			
			// Try to take control of it
			if (isTraversable)
			{
				lastNode.release();
			} else
			{
				m_observers.updateMovement(new Vector2F());

				return m_queryCancel;
			}
		}// If we've just arrived at our target
		else if (deltaMovement.isZero())
		{
			if (atDestination())
			{
				m_observers.updateMovement(deltaMovement);
				nextNode.release();
				return true;
			} else
				return false;
		}

		m_observers.updateMovement(deltaMovement);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_entity.getWorld().getRouteNode(m_entity.getLocation().round()).release();
		m_queryCancel = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#isParallel()
	 */
	@Override
	public final boolean isParallel()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#ignoresPause()
	 */
	@Override
	public final boolean ignoresPause()
	{
		return false;
	}

	protected abstract boolean blocking();

	@Nullable
	protected abstract Vector2F getDestination();

	protected abstract boolean atDestination();
	
	public interface IMovementObserver
	{
		void updateMovement(Vector2F delta);
		void movingTowards(Vector2F target);
	}
	
	private class Observers extends StaticSet<IMovementObserver>
	{
		public void updateMovement(Vector2F delta)
		{
			for(IMovementObserver o : this)
				o.updateMovement(delta);
		}
		
		public void movingTowards(Vector2F target)
		{
			for(IMovementObserver o : this)
				o.movingTowards(target);
		}
	}
}
