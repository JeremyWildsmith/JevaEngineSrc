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

import io.github.jevaengine.Core;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.ResourceFormatException;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.pipeline.Graphic;
import io.github.jevaengine.graphics.pipeline.GraphicRenderHints;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.Entity.IEntityObserver;
import io.github.jevaengine.world.World.WorldConfiguration.TileDeclaration;
import io.github.jevaengine.world.WorldLayer.LayerDeclaration.LayerBackgroundDeclaration.SortedGraphic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashSet;

public class WorldLayer implements IDisposable
{
	private ArrayList<LayerSector> m_sectors;

	private ArrayList<ActorEntry> m_actors;
	
	@Nullable
	private LayerBackground m_background;
	
	public WorldLayer()
	{
		m_sectors = new ArrayList<LayerSector>();
		m_actors = new ArrayList<ActorEntry>();
		m_background = new LayerBackground();
	}

	public static WorldLayer create(World world, TileDeclaration[] tiles, LayerDeclaration layerDecl)
	{
		WorldLayer layer = new WorldLayer();

		if(layerDecl.background != null)
		{
			layer.setBackground(
					new LayerBackground(
							Graphic.create(layerDecl.background.texture),
							Graphic.create(layerDecl.background.colorMap),
							layerDecl.background.location,
							layerDecl.background.graphics));
		}
		
		int locationOffset = 0;
		
		for(int i = 0; i < layerDecl.tileIndices.length; i++)
		{
			int index = layerDecl.tileIndices[i];

			if(index >= 0)
			{
				if(index >= tiles.length)
					throw new ResourceFormatException("Undeclared Tile Declaration Index Used");

				Tile tile = null;
		
				TileDeclaration tileDec1 = tiles[index];

				if(tileDec1.sprite != null)
				{
					Sprite tileSprite = Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration(tileDec1.sprite));
					tileSprite.setAnimation(tileDec1.animation, AnimationState.Play);

					tile = new Tile(tileSprite, tileDec1.isTraversable, tileDec1.allowRenderSplitting, tileDec1.visibility);
				}else
					tile = new Tile(tileDec1.isTraversable, tileDec1.allowRenderSplitting, tileDec1.visibility);

				tile.associate(world);

				tile.setLocation(new Vector2F((locationOffset + i) % world.getWidth(), (float) Math.floor((locationOffset + i) / world.getHeight())));
				layer.add(tile, tileDec1.isStatic);
			}else
				locationOffset += -index - 1;
		}
		
		return layer;
	}
	
	public void dispose()
	{
		for (ActorEntry a : m_actors)
			a.dispose();
		
		m_actors.clear();
	}
	
	public void setBackground(LayerBackground background)
	{
		m_background = background;
	}

	private LayerSector getSector(Vector2F location)
	{
		LayerSectorCoordinate sectorCoordinates = new LayerSectorCoordinate(location);
		
		if (!m_sectors.contains(sectorCoordinates))
			m_sectors.add(new LayerSector(location.round()));

		int sec = m_sectors.indexOf(sectorCoordinates);

		LayerSector sector = m_sectors.get(sec);
		
		return sector;
	}
	
	@Nullable
	private ActorEntry getActor(Actor actor)
	{
		for(ActorEntry entry : m_actors)
		{
			if(entry.m_subject == actor)
				return entry;
		}
		
		return null;
	}
	
	public void add(Actor actor, boolean isStatic)
	{
		m_actors.add(new ActorEntry(actor, isStatic));
	}
	
	public void remove(Actor actor)
	{
		ActorEntry entry = getActor(actor);
		
		if(entry != null)
		{
			entry.dispose();
			m_actors.remove(actor);
		}
	}

	public TileEffects getTileEffects(Vector2D location)
	{
		int index = m_sectors.indexOf(new LayerSectorCoordinate(location));

		if (index < 0)
			return new TileEffects();

		LayerSector sector = m_sectors.get(m_sectors.indexOf(new LayerSectorCoordinate(location)));

		return sector.getTileEffects(location);
	}

	public void update(int delta)
	{
		for (LayerSector sector : m_sectors)
			sector.update(delta);
	}

	void enqueueRender(World parent, Rectangle renderBounds)
	{
		m_background.enqueueRender(parent);
		
		HashSet<Integer> renderSectors = new HashSet<Integer>();

		int sectorX = renderBounds.x / LayerSector.SECTOR_DIMENSIONS;
		int sectorY = renderBounds.y / LayerSector.SECTOR_DIMENSIONS;
		int sectorWidth = (int)Math.ceil((float)renderBounds.width / (float)LayerSector.SECTOR_DIMENSIONS);
		int sectorHeight = (int)Math.ceil((float)renderBounds.height / (float)LayerSector.SECTOR_DIMENSIONS);

		for (int y = sectorY; y <= sectorY + sectorHeight; y++)
		{
			for (int x = sectorX; x <= sectorX + sectorWidth; x++)
			{
				renderSectors.add(m_sectors.indexOf(new LayerSectorCoordinate(x, y, true)));
			}
		}

		for (Integer i : renderSectors)
		{
			if (i >= 0)
				m_sectors.get(i).enqueueRender(renderBounds);
		}
	}

	public static class LayerBackground
	{
		private Graphic m_background;
		private Graphic m_colorMap;
		private Vector2F m_location;
		
		private SortedGraphic[] m_graphics;
		
		public LayerBackground(Graphic background, Graphic colorMap, Vector2F location, SortedGraphic[] graphics)
		{
			m_background = background;
			m_colorMap = colorMap;
			m_location = location;
			m_graphics = graphics;
		}
		
		public LayerBackground()
		{
			this(null, null, new Vector2F(), new SortedGraphic[0]);
		}

		private void enqueueRender(World parent)
		{
			if(m_graphics.length > 0)
			{
				for(SortedGraphic graphic : m_graphics)
				{
					Vector2F graphicLocation = parent.translateScreenToWorld(graphic.origin, 1.0F);
					parent.enqueueRender(new ArtifactRenderer(graphic.colorKey, graphic.origin), graphicLocation.add(m_location));
				}
			}
		}
		
		private class ArtifactRenderer implements IRenderable
		{
			private Color m_key;
			private Vector2D m_offset;
			
			public ArtifactRenderer(Color key, Vector2D offset)
			{
				m_key = key;
				m_offset = offset;
			}
			
			@Override
			public void render(Graphics2D g, int x, int y, float scale)
			{
				if(m_background != null)
				{
					g.setRenderingHint(GraphicRenderHints.KEY_MODE, new GraphicRenderHints.ColorMap(m_colorMap, m_key));
					m_background.render(g, x - m_offset.x, y - m_offset.y, scale);
				}
			}
		}
	}
	
	private class ActorEntry implements IDisposable
	{
		private Actor m_subject;
		private boolean m_isStatic;
		private LocationObserver m_observer = new LocationObserver();
		private LayerSector m_parentSector;
		
		public ActorEntry(Actor subject, boolean isStatic)
		{
			m_subject = subject;
			m_isStatic = isStatic;
			m_parentSector = getSector(subject.getLocation());
			m_parentSector.addActor(subject, isStatic);
			subject.addObserver(m_observer);
		}
		
		@Override
		public void dispose()
		{
			m_parentSector.remove(m_subject);
			m_subject.removeObserver(m_observer);
		}
		
		private class LocationObserver implements IEntityObserver
		{
			@Override
			public void moved()
			{
				if(!m_parentSector.contains(m_subject.getLocation()))
				{
					m_parentSector.remove(m_subject);
					m_parentSector = getSector(m_subject.getLocation());
					m_parentSector.addActor(m_subject, m_isStatic);
				}
			}
			
			@Override
			public void enterWorld() { }
	
			@Override
			public void leaveWorld() { }
	
			@Override
			public void flagSet(String name, int value) { }
	
			@Override
			public void flagCleared(String name) { }
		}
	}
	
	private static class LayerSectorCoordinate
	{
		int x;
		int y;
		boolean isSectorScale;

		public LayerSectorCoordinate(int _x, int _y, boolean _isSectorScale)
		{
			x = _x;
			y = _y;
			isSectorScale = _isSectorScale;
		}
		
		public LayerSectorCoordinate(Vector2F location)
		{
			x = location.round().x;
			y = location.round().y;
		}

		public LayerSectorCoordinate(Vector2D location)
		{
			x = location.x;
			y = location.y;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			if (o == this)
				return true;
			else if (o == null)
				return false;
			else if (o instanceof LayerSectorCoordinate)
			{
				LayerSectorCoordinate coord = (LayerSectorCoordinate) o;

				return coord.x == x && coord.y == y && coord.isSectorScale == isSectorScale;
			}
			else if (o instanceof LayerSector)
			{
				LayerSector sector = (LayerSector) o;
				
				if(isSectorScale)
					return sector.m_location.equals(new Vector2D(x, y));
				else
					return new Rect2D(sector.m_location.x * LayerSector.SECTOR_DIMENSIONS,
							sector.m_location.y * LayerSector.SECTOR_DIMENSIONS,
							LayerSector.SECTOR_DIMENSIONS,
							LayerSector.SECTOR_DIMENSIONS)
							.contains(new Vector2D(x, y));
			} else
				return false;
		}
	}

	private static class LayerSector
	{
		protected static final int SECTOR_DIMENSIONS = 10;

		private Vector2D m_location;

		private ArrayList<Actor> m_dynamic;

		private ArrayList<Actor> m_static;

		private EffectMap m_staticEffectMap;

		private EffectMap m_dynamicEffectMap;

		private boolean m_isDirty;

		public LayerSector(Vector2D containingLocation)
		{
			m_location = new Vector2D(containingLocation.x / SECTOR_DIMENSIONS, containingLocation.y / SECTOR_DIMENSIONS);
			m_dynamic= new ArrayList<Actor>();
			m_static = new ArrayList<Actor>();

			m_staticEffectMap = new EffectMap();
			m_dynamicEffectMap = new EffectMap();

			m_isDirty = false;
		}

		public void addActor(Actor actor, boolean isStatic)
		{
			if(isStatic)
				m_static.add(actor);
			else
				m_dynamic.add(actor);
			
			m_isDirty = true;
		}
		
		public void remove(Actor entity)
		{
			m_dynamic.remove(entity);
			
			if(m_static.contains(entity))
			{
				m_static.remove(entity);
				m_isDirty = true;
			}
		}

		public TileEffects getTileEffects(Vector2D location)
		{
			return m_staticEffectMap.getTileEffects(location).overlay(m_dynamicEffectMap.getTileEffects(location));
		}

		public void update(int deltaTime)
		{
			m_dynamicEffectMap.clear();

			for (Actor a : m_dynamic)
			{
				a.blendEffectMap(m_dynamicEffectMap);
				a.update(deltaTime);
			}

			if (m_isDirty)
			{
				m_staticEffectMap.clear();

				for (Actor a : m_static)
					a.blendEffectMap(m_staticEffectMap);

				m_isDirty = false;
			}
		}

		public void enqueueRender(Rectangle renderBounds)
		{
			for (Actor a : m_static)
			{
				Vector2D location = a.getLocation().round();

				if (renderBounds.contains(new Point(location.x, location.y)))
					a.enqueueRender();
			}
			for (Actor a : m_dynamic)
			{
				Vector2D location = a.getLocation().floor();

				if (renderBounds.contains(new Point(location.x, location.y)))
					a.enqueueRender();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			if (o == this)
				return true;
			else if (o == null)
				return false;
			else if (o instanceof Vector2D || o instanceof Vector2F)
			{
				Vector2D src = (o instanceof Vector2F) ? ((Vector2F) o).floor() : ((Vector2D) o);
				return new Rect2D(m_location.x * SECTOR_DIMENSIONS, m_location.y * SECTOR_DIMENSIONS, SECTOR_DIMENSIONS, SECTOR_DIMENSIONS).contains(src);
			} else
				return false;
		}
		
		public boolean contains(Vector2F location)
		{
			return new Rect2D(m_location.x * SECTOR_DIMENSIONS, m_location.y * SECTOR_DIMENSIONS, SECTOR_DIMENSIONS, SECTOR_DIMENSIONS).contains(location.floor());
		}
	}
	
	public static class LayerDeclaration implements ISerializable
	{
		public int[] tileIndices = new int[0];
		
		@Nullable 
		public LayerBackgroundDeclaration background;
		
		public LayerDeclaration() { }

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("indice").setValue(this.tileIndices);
			
			if(background != null)
				target.addChild("background").setValue(background);
		}

		@Override
		public void deserialize(IImmutableVariable source)
		{
			Integer indice[] = source.getChild("indice").getValues(Integer[].class);
			
			this.tileIndices = new int[indice.length];
			
			for(int i = 0; i < indice.length; i++)
				this.tileIndices[i] = indice[i];
			
			if(source.childExists("background"))
				background = source.getChild("background").getValue(LayerBackgroundDeclaration.class);
		}
		
		public static class LayerBackgroundDeclaration implements ISerializable
		{
			public String texture;
			public String colorMap;
			public Vector2F location;
			public SortedGraphic graphics[];
			
			public LayerBackgroundDeclaration()
			{
				location = new Vector2F();
			}

			@Override
			public void serialize(IVariable target)
			{
				if(texture != null && texture.length() > 0 &&
					colorMap != null && colorMap.length() > 0 &&
					graphics != null && graphics.length > 0)
				{
					target.addChild("texture").setValue(texture);
					target.addChild("colorMap").setValue(colorMap);
					target.addChild("location").setValue(location);
					target.addChild("graphics").setValue(graphics);
				}
			}

			@Override
			public void deserialize(IImmutableVariable source)
			{
				if(source.childExists("texture") &&
					source.childExists("colorMap") &&
					source.childExists("graphics"))
				{
					texture = source.getChild("texture").getValue(String.class);
					colorMap = source.getChild("colorMap").getValue(String.class);
					location = source.getChild("location").getValue(Vector2F.class);
					graphics = source.getChild("graphics").getValues(SortedGraphic[].class);
				}
			}
			
			public static class SortedGraphic implements ISerializable
			{
				private Color colorKey;
				private Vector2D origin;
				
				public SortedGraphic() {}

				@Override
				public void serialize(IVariable target)
				{
					if(colorKey != null && origin != null)
					{
						IVariable colorKeyVar = target.addChild("colorKey");
						colorKeyVar.addChild("r").setValue(colorKey.getRed());
						colorKeyVar.addChild("g").setValue(colorKey.getGreen());
						colorKeyVar.addChild("b").setValue(colorKey.getBlue());
						
						target.addChild("origin").setValue(origin);
						
					}
				}

				@Override
				public void deserialize(IImmutableVariable source)
				{
					IImmutableVariable colorKeyVar = source.getChild("colorKey");
					
					int r = colorKeyVar.getChild("r").getValue(Integer.class);
					int g = colorKeyVar.getChild("g").getValue(Integer.class);
					int b = colorKeyVar.getChild("b").getValue(Integer.class);
					
					colorKey = new Color(r, g, b);
					origin = source.getChild("origin").getValue(Vector2D.class);
				}
			}
		}
	}
}
