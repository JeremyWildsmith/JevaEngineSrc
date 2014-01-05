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

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.Entity.IEntityObserver;

public final class FollowCamera implements ICamera
{

	private World m_world;

	private String m_targetEntity;

	private Entity m_target;

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
		if (m_world.getEntity(m_targetEntity) == null)
			return new Vector2D();

		if (m_target == null)
		{
			m_target = m_world.getEntity(m_targetEntity);
			
			if(m_target != null)
				m_target.addObserver(new EntityObserver());
		}

		if (m_target == null)
			return new Vector2D();
		else
			return m_world.translateWorldToScreen(m_target.getLocation(), getScale());
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

	private class EntityObserver implements IEntityObserver
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
		 * @see io.github.jeremywildsmith.jevaengine.world.Entity.IEntityObserver#enterWorld()
		 */
		@Override
		public void enterWorld() { }

		@Override
		public void replaced() { }

		@Override
		public void flagSet(String name, int value) { }

		@Override
		public void flagCleared(String name) { }
	}

}
