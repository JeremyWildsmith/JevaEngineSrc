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
import io.github.jevaengine.world.scene.ISceneBufferFactory;

public final class ControlledCamera extends SceneBufferCamera
{
	private Vector3F m_lookAtTile;
	
	private float m_zoom = 1.0F;
	
	public ControlledCamera(ISceneBufferFactory sceneBufferFactory)
	{
		super(sceneBufferFactory);
		m_lookAtTile = new Vector3F();
	}

	public float getZoom()
	{
		return m_zoom;
	}
	
	public void lookAt(Vector3F tileLocation)
	{
		m_lookAtTile = new Vector3F(tileLocation);
	}

	public void move(Vector3F delta)
	{
		m_lookAtTile = m_lookAtTile.add(delta);
	}

	@Override
	public Vector3F getLookAt()
	{
		return m_lookAtTile;
	}

	@Override
	protected void onAttach() { }

	@Override
	protected void onDettach() { }
}
