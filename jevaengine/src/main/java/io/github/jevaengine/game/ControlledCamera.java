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
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.World;

public final class ControlledCamera implements ICamera
{
	@Nullable
	private World m_world;
	
	private Vector2F m_lookAtTile;

	private Vector2D m_lookAtScreen;
	
	private float m_zoom = 1.0F;
	
	public ControlledCamera()
	{
		m_lookAtTile = new Vector2F();
		m_lookAtScreen = new Vector2D();
	}

	public ControlledCamera(Vector2F tileLocation)
	{
		lookAt(tileLocation);
	}

	public void setZoom(float zoom)
	{
		m_zoom = zoom;
		lookAt(m_lookAtTile);
	}
	
	public float getZoom()
	{
		return m_zoom;
	}
	
	public void lookAt(Vector2F tileLocation)
	{
		if (m_world != null)
		{
			float fX = Math.min(Math.max(0, tileLocation.x), m_world.getWidth() - 1);
			float fY = Math.min(Math.max(0, tileLocation.y), m_world.getHeight() - 1);
			m_lookAtTile = new Vector2F(fX, fY);

			m_lookAtScreen = m_world.translateWorldToScreen(m_lookAtTile, getScale());
		} else
			m_lookAtTile = tileLocation;
	}

	public void move(Vector2F delta)
	{
		lookAt(m_lookAtTile.add(delta));
	}

	@Override
	public Vector2D getLookAt()
	{
		return m_lookAtScreen;
	}

	@Override
	public World getWorld()
	{
		return m_world;
	}

	@Override
	public void attach(World world)
	{
		dettach();
		
		m_world = world;
		// Refresh target tile with current world
		lookAt(m_lookAtTile);
	}

	@Override
	public void dettach()
	{
		m_world = null;
	}

	@Override
	public float getScale()
	{
		return m_zoom;
	}
}
