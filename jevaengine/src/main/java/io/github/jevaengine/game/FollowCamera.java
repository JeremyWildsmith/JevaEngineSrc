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

import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.IEntity.IEntityObserver;
import io.github.jevaengine.world.scene.ISceneBufferFactory;

import java.lang.ref.WeakReference;

public final class FollowCamera extends SceneBufferCamera
{
	private WeakReference<IEntity> m_target;	
	private EntityObserver m_observer = new EntityObserver();
	
	public FollowCamera(ISceneBufferFactory sceneBufferFactory)
	{
		super(sceneBufferFactory);
		m_target = new WeakReference<IEntity>(null);
	}

	public void setTarget(@Nullable IEntity target)
	{
		if(m_target.get() != null)
			m_target.get().removeObserver(m_observer);
		
		m_target = new WeakReference<>(target);
		
		if(target != null)
			target.addObserver(m_observer);
	}

	@Override
	public Vector3F getLookAt()
	{
		if (m_target.get() == null)
			return new Vector3F();
		else
			return m_target.get().getBody().getLocation();
	}

	@Override
	protected void onAttach() { }
	
	@Override
	public void onDettach()
	{
		setTarget(null);
	}
	
	private class EntityObserver implements IEntityObserver
	{
		@Override
		public void leaveWorld()
		{
			setTarget(null);
		}

		@Override
		public void enterWorld() { }

		@Override
		public void flagSet(String name, int value) { }

		@Override
		public void flagCleared(String name) { }
	}
}
