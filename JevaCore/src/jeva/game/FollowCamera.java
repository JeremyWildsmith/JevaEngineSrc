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
package jeva.game;

import jeva.config.Variable;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.Actor;
import jeva.world.Entity;
import jeva.world.World;
import jeva.world.Actor.IActorObserver;
import jeva.world.WorldDirection;

/**
 * The Class FollowCamera.
 */
public final class FollowCamera implements IWorldCamera
{

	/** The m_world. */
	private World m_world;

	/** The m_target entity. */
	private String m_targetEntity;

	/** The m_target. */
	private Actor m_target;

	/** The m_target location. */
	private Vector2D m_targetLocation;

	/**
	 * Instantiates a new follow camera.
	 */
	public FollowCamera()
	{
		m_targetEntity = null;
	}

	/**
	 * Sets the target.
	 * 
	 * @param target
	 *            the new target
	 */
	public void setTarget(String target)
	{
		m_targetEntity = target;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#getLookAt()
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#getScale()
	 */
	@Override
	public float getScale()
	{
		return 1.0F;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#attach(jeva.world.World)
	 */
	@Override
	public void attach(World world)
	{
		m_world = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#dettach()
	 */
	@Override
	public void dettach()
	{
		m_world = null;
	}

	/**
	 * An asynchronous update interface for receiving notifications about Actor
	 * information as the Actor is constructed.
	 */
	private class ActorObserver implements IActorObserver
	{
		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Entity.IEntityObserver#leaveWorld()
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
		 * @see jeva.world.Actor.IActorObserver#placement(jeva.math.Vector2F)
		 */
		@Override
		public void placement(Vector2F location)
		{
			m_targetLocation = m_world.translateWorldToScreen(m_target.getLocation(), getScale());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Actor.IActorObserver#moved(jeva.math.Vector2F)
		 */
		@Override
		public void moved(Vector2F delta)
		{
			m_targetLocation = m_world.translateWorldToScreen(m_target.getLocation(), getScale());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Entity.IEntityObserver#enterWorld()
		 */
		@Override
		public void enterWorld()
		{
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Entity.IEntityObserver#taskBusyState(boolean)
		 */
		@Override
		public void taskBusyState(boolean isBusy)
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
