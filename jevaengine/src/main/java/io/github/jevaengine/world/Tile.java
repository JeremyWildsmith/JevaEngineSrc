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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.world;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.EffectMap.TileEffects;

public final class Tile extends Actor
{
	@Nullable
	private Sprite m_sprite;

	private boolean m_isTraversable;

	private boolean m_enableSplitting;
	
	private float m_visiblityObstruction;

	public Tile(@Nullable Sprite sprite, boolean isTraversable, boolean enableSplitting, float fVisibilityObstruction)
	{
		m_sprite = sprite;
		m_isTraversable = isTraversable;
		m_enableSplitting = enableSplitting;
		m_visiblityObstruction = fVisibilityObstruction;
	}
	
	public Tile(boolean isTraversable, boolean enableSplitting, float fVisibilityObstruction)
	{
		this(null, isTraversable, enableSplitting, fVisibilityObstruction);
	}

	public Sprite getSprite()
	{
		return m_sprite;
	}
	
	public void setSprite(@Nullable Sprite sprite)
	{
		m_sprite = sprite;
	}
	
	protected final void setVisibilityObstruction(float fObstruction)
	{
		m_visiblityObstruction = fObstruction;
	}

	protected final float getVisibilityObstruction()
	{
		return m_visiblityObstruction;
	}

	@Override
	public int getTileWidth()
	{
		if(m_sprite == null)
			return 0;
		else if (!m_enableSplitting)
			return 1;

		// get tile width
		Vector2F v = getWorld().getPerspectiveMatrix(1.0F).dot(new Vector2F(1, -1));
		return (int) (Math.ceil((m_sprite.getBounds().width) / v.x));
	}

	@Override
	public int getTileHeight()
	{
		if(m_sprite == null)
			return 0;
		else if (!m_enableSplitting)
			return 1;

		// get tile height
		Vector2F v = getWorld().getPerspectiveMatrix(1.0F).dot(new Vector2F(0, 2));
		return (int) (Math.ceil((m_sprite.getBounds().height) / v.y));
	}

	@Override
	public void blendEffectMap(EffectMap globalEffectMap)
	{
		globalEffectMap.applyOverlayEffects(this.getLocation().round(), new TileEffects(m_isTraversable).overlay(new TileEffects(m_visiblityObstruction)));
	}

	@Override
	public void doLogic(int deltaTime)
	{
		m_sprite.update(deltaTime);
	}
	
	@Override
	public float getVisibilityFactor()
	{
		return 0;
	}

	@Override
	public float getViewDistance()
	{
		return 0;
	}

	@Override
	public float getFieldOfView()
	{
		return 0;
	}

	@Override
	public float getVisualAcuity()
	{
		return 0;
	}

	@Override
	public float getSpeed()
	{
		return 0;
	}

	@Override
	public WorldDirection[] getAllowedMovements()
	{
		return new WorldDirection[]
		{ WorldDirection.Zero };
	}
	
	@Override
	public Rect2F getGraphicBounds(float scale)
	{
		return m_sprite.getBounds(scale);
	}
	
	@Override
	@Nullable
	public IRenderable getGraphic()
	{
		return m_sprite;
	}
}
