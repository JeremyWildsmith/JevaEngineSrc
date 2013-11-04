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
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.config.VariableStore;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.EffectMap;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.IInteractable;
import io.github.jevaengine.world.Tile;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.WorldDirection;
import io.github.jevaengine.world.WorldLayer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;

public class EditorTile implements IInteractable
{
	private String m_spriteName;
	private boolean m_isTraversable;
	private boolean m_isStatic;
	private boolean m_enablesSplitting;

	private ContainedTile m_tile;
	private String m_animation;

	public EditorTile(String spriteName, WorldDirection direction, String animation, boolean isTraversable, boolean isStatic, boolean enableSplitting, float fVisiblity)
	{
		m_tile = new ContainedTile(Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(spriteName))), direction, animation, true, fVisiblity);

		m_isStatic = isStatic;
		m_isTraversable = isTraversable;
		m_enablesSplitting = enableSplitting;
		m_spriteName = spriteName;
		m_animation = animation;
	}

	public EditorTile(Sprite nullTileSprite, WorldDirection direction, String animation, boolean isTraversable, boolean isStatic, boolean enableSplitting, float fVisiblity)
	{
		m_tile = new ContainedTile(nullTileSprite, direction, animation, true, fVisiblity);

		m_isStatic = isStatic;
		m_isTraversable = isTraversable;
		m_enablesSplitting = enableSplitting;
		m_spriteName = "";
		m_animation = animation;
	}

	public void setVisibilityObstruction(float fVisiblity)
	{
		m_tile.setVisibilityObstruction(fVisiblity);
	}

	public float getVisibilityObstruction()
	{
		return m_tile.getVisibilityObstruction();
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

	public void setSpriteName(String spriteName)
	{
		m_spriteName = spriteName;
		m_tile.setSprite(spriteName);
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
		m_tile.setAnimation(animation);
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
		layer.addStaticTile(m_tile);
	}

	public void setLocation(Vector2D location)
	{
		m_tile.setLocation(new Vector2F(location));
	}

	public Vector2D getLocation()
	{
		return m_tile.getLocation().round();
	}

	public void setDirection(WorldDirection direction)
	{
		m_tile._setDirection(direction);
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

	private class ContainedTile extends Tile
	{
		public ContainedTile(Sprite sprite, WorldDirection direction, String animation, boolean isTraversable, float fVisiblity)
		{
			super(sprite, direction, animation, isTraversable, false, fVisiblity);
		}

		@Override
		public void blendEffectMap(EffectMap globalEffectMap)
		{
			globalEffectMap.applyOverlayEffects(getLocation().round(), new TileEffects(EditorTile.this));
		}

		protected void _setDirection(WorldDirection direction)
		{
			super.setDirection(direction);
		}

		protected void setAnimation(String animation)
		{
			super.setAnimation(animation);
		}

		protected void setSprite(String spriteName)
		{
			super.setSprite(Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(spriteName))));
		}

		protected void setVisibilityObstruction(float fVisiblity)
		{
			super.setVisibilityObstruction(fVisiblity);
		}

		protected float getVisibilityObstruction()
		{
			return super.getVisibilityObstruction();
		}

		@Override
		public IRenderable[] getGraphics()
		{
			if (!m_isTraversable)
			{
				ArrayList<IRenderable> renderables = new ArrayList<IRenderable>();
				renderables.addAll(Arrays.asList(super.getGraphics()));

				renderables.add(new IRenderable()
				{

					@Override
					public void render(Graphics2D g, int x, int y, float fScale)
					{
						g.setColor(Color.red);
						g.drawRect(x - 2, y - 2, 2, 2);
					}
				});

				return renderables.toArray(new IRenderable[renderables.size()]);
			} else
				return super.getGraphics();
		}
	}

	@Override
	public String[] getCommands()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void doCommand(String bommand)
	{
		// TODO Auto-generated method stub

	}

}
