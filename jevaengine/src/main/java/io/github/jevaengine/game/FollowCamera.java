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
package io.github.jevaengine.game;

import io.github.jevaengine.config.Variable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.WorldDirection;
import io.github.jevaengine.world.Actor.IActorObserver;

public final class FollowCamera implements ICamera
{

	private World m_world;

	private String m_targetEntity;

	private Actor m_target;

	private Vector2D m_targetLocation;

	public FollowCamera()
	{
		m_targetEntity = null;
	}

	public void setTarget(String target)
	{
		m_targetEntity = target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#getLookAt()
	 */
	@Override
	public Vector2D getLookAt()
	{
		if (m_world == null || m_targetEntity == null || !m_world.variableExists(m_targetEntity))
			return new Vector2D();

		if (m_target == null)
		{
			Variable v = m_world.getVariable(m_targetEntity);

			if (v instanceof Actor)
				m_target = (Actor) v;

			m_target.addObserver(new ActorObserver());
			m_targetLocation = m_world.translateWorldToScreen(m_target.getLocation(), getScale());
		}

		if (m_target == null)
			return new Vector2D();
		else
			return m_targetLocation;
	}

	@Override
	public World getWorld()
	{
		return m_world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#getScale()
	 */
	@Override
	public float getScale()
	{
		return 1.0F;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#attach(jeva.world.World)
	 */
	@Override
	public void attach(World world)
	{
		m_world = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#dettach()
	 */
	@Override
	public void dettach()
	{
		m_world = null;
	}

	private class ActorObserver implements IActorObserver
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.world.Entity.IEntityObserver#leaveWorld()
		 */
		@Override
		public void leaveWorld()
		{
			m_target.removeObserver(this);
			m_target = null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.world.Actor.IActorObserver#placement(jeva.math.Vector2F)
		 */
		@Override
		public void placement(Vector2F location)
		{
			m_targetLocation = m_world.translateWorldToScreen(m_target.getLocation(), getScale());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.world.Actor.IActorObserver#moved(jeva.math.Vector2F)
		 */
		@Override
		public void moved(Vector2F delta)
		{
			m_targetLocation = m_world.translateWorldToScreen(m_target.getLocation(), getScale());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.world.Entity.IEntityObserver#enterWorld()
		 */
		@Override
		public void enterWorld()
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Actor.IActorObserver#directionChanged(jeva.world.
		 * WorldDirection)
		 */
		@Override
		public void directionChanged(WorldDirection direction)
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jeva.world.DialogicalEntity.IDialogueObserver#onDialogEvent(jeva.
		 * world.Entity, int)
		 */
		@Override
		public void onDialogEvent(Entity subject, int event)
		{
		}
	}

}
