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

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;

import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.IActorRenderFilter;
import io.github.jevaengine.world.World.WorldRenderEntry;

public final class ControlledCamera implements ICamera
{
	@Nullable private World m_world;
	
	private Vector2F m_lookAtTile;

	private Vector2D m_lookAtScreen;
	
	private float m_zoom = 1.0F;

	private ActorFilter m_actorFilter = new ActorFilter();
	
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#getLookAt()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#attach(jeva.world.World)
	 */
	@Override
	public void attach(World world)
	{
		dettach();
		
		m_world = world;
		m_world.addActorRenderFilter(m_actorFilter);
		// Refresh target tile with current world
		lookAt(m_lookAtTile);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#dettach()
	 */
	@Override
	public void dettach()
	{
		if(m_world != null)
			m_world.removeActorRenderFilter(m_actorFilter);
		
		m_world = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.game.IWorldCamera#getScale()
	 */
	@Override
	public float getScale()
	{
		return m_zoom;
	}
	
	private class ActorFilter implements IActorRenderFilter
	{
		private static final int VIEW_BOUNDS = 300;
		private AlphaComposite m_alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
		private Composite m_oldComposite;

		private Graphics2D m_graphics;
		private float m_scale;
		private Rect2F m_viewBounds;
		
		@Override
		public void beginBatch(Graphics2D g, float scale)
		{
			m_graphics = g;
			m_scale = scale;
			
			m_viewBounds = new Rect2F(m_lookAtScreen.x - VIEW_BOUNDS / 2,
										m_lookAtScreen.y,
										VIEW_BOUNDS,
										VIEW_BOUNDS);
		}

		@Override
		public void begin(WorldRenderEntry e)
		{
			Actor dispatcher = e.getDispatcher();
			
			if(dispatcher == null || e.getLayer() != m_world.getEntityLayer())
				return;
			
			Vector2D location = m_world.translateWorldToScreen(dispatcher.getLocation(), m_scale);
			Rect2F renderBounds = dispatcher.getGraphicBounds(m_scale).add(location);
			
			if(renderBounds.intersects(m_viewBounds))
			{
				m_oldComposite = m_graphics.getComposite();
				m_graphics.setComposite(m_alphaComposite);
			}
		}
		
		@Override
		public void end()
		{
			if(m_oldComposite != null)
			{
				m_graphics.setComposite(m_oldComposite);
				m_oldComposite = null;
			}
		}

		@Override
		public void endBatch() { }
	}
}
