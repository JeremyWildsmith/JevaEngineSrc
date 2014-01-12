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
package io.github.jevaengine.mapeditor;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.EffectMap;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.IInteractable;
import io.github.jevaengine.world.Tile;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.WorldDirection;
import io.github.jevaengine.world.WorldLayer;

import java.awt.Color;
import java.awt.Graphics2D;

public class EditorTile implements IInteractable
{
	private String m_spriteName;
	private boolean m_isTraversable;
	private boolean m_isStatic;
	private boolean m_enablesSplitting;

	private ContainedTile m_tile;
	private String m_animation;
	
	private float m_visObstruction;

	public EditorTile(String spriteName, String animation, boolean isTraversable, boolean isStatic, boolean enableSplitting, float fVisiblity)
	{
		Sprite sprite = Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration(spriteName));
		sprite.setAnimation(animation, AnimationState.Play);
		
		m_tile = new ContainedTile(sprite, true, fVisiblity);

		m_isStatic = isStatic;
		m_isTraversable = isTraversable;
		m_enablesSplitting = enableSplitting;
		m_spriteName = spriteName;
		m_animation = animation;
	}

	public void setVisibilityObstruction(float fVisiblity)
	{
		m_visObstruction = fVisiblity;
	}

	public float getVisibilityObstruction()
	{
		return m_visObstruction;
	}

	public void setTraversable(boolean isTraversable)
	{
		m_isTraversable = isTraversable;
	}

	public boolean isTraversable()
	{
		return m_isTraversable;
	}

	public void setStatic(boolean isStatic)
	{
		m_isStatic = isStatic;
	}

	public boolean isStatic()
	{
		return m_isStatic;
	}

	public void setSpriteName(String spriteName, String animation)
	{
		m_spriteName = spriteName;
		m_animation = animation;
		
		Sprite sprite = Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration(spriteName));
		sprite.setAnimation(animation, AnimationState.Play);
		
		m_tile.setSprite(sprite);	
	}

	public boolean enablesSplitting()
	{
		return m_enablesSplitting;
	}

	public void setEnableSplitting(boolean enableSplitting)
	{
		m_enablesSplitting = enableSplitting;
	}

	public String getSpriteName()
	{
		return m_spriteName;
	}

	public void setSpriteAnimation(String animation)
	{
		m_animation = animation;
		m_tile.getSprite().setAnimation(animation, AnimationState.Play);
	}

	public String getSpriteAnimation()
	{
		return m_animation;
	}

	public void addToWorld(World world, WorldLayer layer)
	{
		if (m_tile.isAssociated())
			m_tile.disassociate();

		m_tile.associate(world);
		
		layer.add(m_tile, true);
	}

	public void setLocation(Vector2D location)
	{
		m_tile.setLocation(new Vector2F(location));
	}

	public Vector2D getLocation()
	{
		return m_tile.getLocation().round();
	}

	public WorldDirection getDirection()
	{
		return m_tile.getDirection();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		else if (!(o instanceof EditorTile))
			return false;

		EditorTile tile = (EditorTile) o;

		return (tile.m_isStatic == this.m_isStatic && tile.m_isTraversable == this.m_isTraversable && tile.m_enablesSplitting == this.m_enablesSplitting && tile.getVisibilityObstruction() == this.getVisibilityObstruction() && tile.getDirection() == this.getDirection() && tile.getSpriteName().compareTo(this.getSpriteName()) == 0 && tile.getSpriteAnimation().compareTo(this.getSpriteAnimation()) == 0);
	}

	@Override
	public String getDefaultCommand()
	{
		return null;
	}
	
	@Override
	public String[] getCommands()
	{
		return new String[0];
	}

	@Override
	public void doCommand(String bommand) { }

	private class ContainedTile extends Actor
	{
		private Tile m_contained;
		
		public ContainedTile(Sprite sprite, boolean isTraversable, float fVisiblity)
		{
			m_contained = new Tile(sprite, isTraversable, false, fVisiblity);
		}

		@Override
		public void blendEffectMap(EffectMap globalEffectMap)
		{
			globalEffectMap.applyOverlayEffects(getLocation().round(), new TileEffects(EditorTile.this));
		}

		Sprite getSprite()
		{
			return m_contained.getSprite();
		}
		
		void setSprite(Sprite sprite)
		{
			m_contained.setSprite(sprite);
		}

		@Override
		public Rect2F getGraphicBounds(float scale)
		{
			return m_contained.getGraphicBounds(scale);
		}

		@Override
		public IRenderable getGraphic()
		{
			return new IRenderable()
			{
				@Override
				public void render(Graphics2D g, int x, int y, float fScale)
				{
					m_contained.getGraphic().render(g, x, y, fScale);
					
					if(!m_isTraversable)
					{
						g.setColor(Color.red);
						g.drawRect(x - 2, y - 2, 2, 2);
					}
				}
			};
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
		public int getTileWidth()
		{
			return 1;
		}

		@Override
		public int getTileHeight()
		{
			return 1;
		}

		@Override
		public WorldDirection[] getAllowedMovements()
		{
			return new WorldDirection[0];
		}

		@Override
		public void doLogic(int deltaTime) { }
	}
}
