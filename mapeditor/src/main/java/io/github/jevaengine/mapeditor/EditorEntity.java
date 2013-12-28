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
import io.github.jevaengine.game.Game;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Font;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.Text;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.WorldDirection;
import java.awt.Color;
import java.awt.Graphics2D;

public class EditorEntity
{
	private Entity m_containedEntity;

	private String m_name;
	private String m_className;
	private String m_config;
	
	private WorldDirection m_direction;
	private Vector2F m_location;

	private Sprite m_tileSprite;
	
	public EditorEntity(String name, String className, String config)
	{
		m_name = name;
		m_className = className;
		m_config = config;
		m_location = new Vector2F();
		m_direction = WorldDirection.Zero;
		m_tileSprite = Sprite.create(Core.getService(IResourceLibrary.class).openConfiguration("@tile/tile.jsf"));
		m_tileSprite.setAnimation("entity", AnimationState.Play);
	}
	
	public void setName(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public void setClassName(String className)
	{
		m_className = className;
	}

	public String getClassName()
	{
		return m_className;
	}

	public String getConfig()
	{
		return m_config;
	}

	public void setConfig(String config)
	{
		m_config = config;
	}
	
	public Vector2F getLocation()
	{
		return m_location;
	}
	
	public void setLocation(Vector2F location)
	{
		m_location = location;
		
		if(m_containedEntity != null)
			m_containedEntity.setLocation(location);
	}
	
	public WorldDirection getDirection()
	{
		return m_direction;
	}
	
	public void setDirection(WorldDirection direction)
	{
		m_direction = direction;
		
		if(m_containedEntity != null)
			m_containedEntity.setDirection(direction);
	}

	public void refresh(World world)
	{
		if (m_containedEntity != null)
			world.removeEntity(m_containedEntity);

		m_containedEntity = new EntityDummy();
		m_containedEntity.setLocation(m_location);
		m_containedEntity.setDirection(m_direction);
		
		world.addEntity(m_containedEntity);
	}

	public void remove(World world)
	{
		if (m_containedEntity != null)
			world.removeEntity(m_containedEntity);
	}

	@Override
	public String toString()
	{
		return String.format("%s of %s", m_name, m_className + (m_config.length() > 0 ? " with " + m_config : ""));
	}
	
	private class EntityDummy extends Actor
	{
		Text m_text;
		
		public EntityDummy()
		{
			Font font = Core.getService(Game.class).getGameStyle().getFont(Color.yellow);
			m_text = new Text(EditorEntity.this.getName(), font, 1.0F);
		}
		
		@Override
		public IRenderable getGraphic()
		{
			return new IRenderable()
			{

				@Override
				public void render(Graphics2D g, int x, int y, float fScale)
				{
					m_tileSprite.render(g, x, y, fScale);
					m_text.render(g, x, y - 15, fScale);
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
