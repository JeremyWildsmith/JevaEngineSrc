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

import com.sun.istack.internal.Nullable;

import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.World;


public final class ControlledCamera implements IWorldCamera
{

	/** The m_world. */
	@Nullable private World m_world;

	/** Tile Offset */
	private Vector2F m_lookAtTile;
	
	/** Screen Offset */
	private Vector2D m_lookAtScreen;

	
	public ControlledCamera()
	{
		m_lookAtTile = new Vector2F();
		m_lookAtScreen = new Vector2D();
	}

	
	public ControlledCamera(Vector2F tileLocation)
	{
		lookAt(tileLocation);
	}

	
	public void lookAt(Vector2F tileLocation)
	{
		if(m_world != null)
		{
			float fX = Math.min(Math.max(0, tileLocation.x), m_world.getWidth() - 1);
			float fY = Math.min(Math.max(0, tileLocation.y), m_world.getHeight() - 1);
			m_lookAtTile = new Vector2F(fX, fY);
			
			m_lookAtScreen = m_world.translateWorldToScreen(m_lookAtTile, getScale());
		}else
			m_lookAtTile = tileLocation;
	}

	
	public void move(Vector2F delta)
	{
		lookAt(m_lookAtTile.add(delta));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#getLookAt()
	 */
	@Override
	public Vector2D getLookAt()
	{
		return m_lookAtScreen;
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
		
		//Refresh target tile with current world
		lookAt(m_lookAtTile);
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
}
