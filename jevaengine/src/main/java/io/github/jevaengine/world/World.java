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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.script.ScriptException;

import io.github.jevaengine.Core;
import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.Script;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.game.ResourceLoadingException;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.math.Matrix2X2;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.Entity.EntityBridge;
import io.github.jevaengine.world.World.WorldConfiguration.EntityDeclaration;
import io.github.jevaengine.world.World.WorldConfiguration.LayerDeclaration;
import io.github.jevaengine.world.World.WorldConfiguration.TileDeclaration;

public final class World implements IDisposable
{
	private static final int WORLD_TICK_INTERVAL = 500;

	private final Observers m_observers = new Observers();

	// Read comments in update route for notes on m_additionEntities member.
	private ArrayList<Entity> m_additionEntities = new ArrayList<Entity>();
	private StaticSet<Entity> m_entities;

	private TreeMap<Vector3F, ArrayList<WorldRenderEntry>> m_renderQueue;
	private ArrayList<IActorRenderFilter> m_renderFilters;
	private float m_renderQueueDepth;

	private HashMap<Vector2D, RouteNode> m_routeNodes;
	
	private ArrayList<WorldLayer> m_layers;
	private int m_entityLayer;
	
	private SceneLighting m_worldLighting;
	private EffectMap m_entityEffectMap;
	private WorldScriptManager m_worldScript;

	private int m_worldWidth;
	private int m_worldHeight;
	private int m_tileWidth;
	private int m_tileHeight;

	private final Matrix2X2 m_worldToScreenMatrix;

	private boolean m_isPaused = false;

	private Calendar m_worldTime;

	private float m_fTimeMultiplier;

	private int m_timeSinceTick;

	public World(int width, int height, int tileWidth, int tileHeight, int entityLayer, @Nullable String worldScript)
	{
		m_layers = new ArrayList<WorldLayer>();
		m_entities = new StaticSet<Entity>();
		m_renderFilters = new ArrayList<IActorRenderFilter>();
		m_renderQueue = new TreeMap<Vector3F, ArrayList<WorldRenderEntry>>();
		m_renderQueueDepth = 0.0F;

		m_routeNodes = new HashMap<Vector2D, RouteNode>();

		m_entityEffectMap = new EffectMap(new Rectangle(0, 0, width, height));

		m_worldWidth = width;
		m_worldHeight = height;
		m_tileWidth = tileWidth;
		m_tileHeight = tileHeight;

		m_entityLayer = entityLayer;

		m_worldToScreenMatrix = new Matrix2X2(tileWidth / 2.0F, -tileWidth / 2.0F, tileHeight / 2.0F, tileHeight / 2.0F);

		m_worldLighting = new SceneLighting();

		m_worldTime = Calendar.getInstance();
		m_fTimeMultiplier = 1.0F;
		m_timeSinceTick = 0;

		if (worldScript != null)
			m_worldScript = new WorldScriptManager(Core.getService(ResourceLibrary.class).openScript(worldScript, new WorldScriptContext()));
		else
			m_worldScript = new WorldScriptManager();

		m_worldScript.onEnter();
	}

	public World(int width, int height, int tileWidth, int tileHeight, int entityLayerDepth)
	{
		this(width, height, tileWidth, tileHeight, entityLayerDepth, null);
	}

	public static World create(IVariable source) throws ResourceLoadingException
	{
		WorldConfiguration worldConfig = source.getValue(WorldConfiguration.class);

		World world = new World(worldConfig.worldWidth, worldConfig.worldHeight, 
									worldConfig.tileWidth, worldConfig.tileHeight, 
									worldConfig.entityLayer, worldConfig.script);
		
		HashMap<Integer, IVariable> tileSpriteDefinitions = new HashMap<Integer, IVariable>();
			
		for (LayerDeclaration layerDeclaration : worldConfig.layers)
		{
			WorldLayer worldLayer = new WorldLayer();
			int[] tileIndices = layerDeclaration.tileIndices;
			
			int locationOffset = 0;
			for (int i = 0; i < tileIndices.length; i++)
			{
				if (tileIndices[i] >= 0)
				{
					if (tileIndices[i] >= worldConfig.tiles.length)
						throw new ResourceLoadingException("Undeclared Tile Class Index Used");

					TileDeclaration tileDecl = worldConfig.tiles[tileIndices[i]];
					
					if(!tileSpriteDefinitions.containsKey(tileIndices[i]))
						tileSpriteDefinitions.put(tileIndices[i], Core.getService(ResourceLibrary.class).openConfiguration(tileDecl.sprite));

					Sprite tileSprite = Sprite.create(tileSpriteDefinitions.get(tileIndices[i]));
					tileSprite.setAnimation(tileDecl.animation, AnimationState.Play);
					
					Tile tile = new Tile(tileSprite, tileDecl.isTraversable,  
										tileDecl.allowRenderSplitting, tileDecl.visibility);
					
					tile.associate(world);

					// Location of tile must be set _BEFORE_ adding it to map layer
					// so map layer can place the tile in the proper sector
					// for optimizations.
					tile.setLocation(new Vector2F((locationOffset + i) % world.m_worldWidth, (float) Math.floor((locationOffset + i) / world.m_worldWidth)));

					worldLayer.add(tile, tileDecl.isStatic);
				}else
					locationOffset += Math.abs(tileIndices[i]) - 1;
			}
			
			world.m_layers.add(worldLayer);
		}
		
		for (EntityDeclaration entityConfig : worldConfig.entities)
		{
			ResourceLibrary resourceLib = Core.getService(ResourceLibrary.class);
			
			Entity entity = resourceLib.createEntity(resourceLib.lookupEntity(entityConfig.type), entityConfig.name, entityConfig.config);
			
			world.addEntity(entity);
			
			if(entityConfig.location != null)
				entity.setLocation(entityConfig.location);
			
			if(entityConfig.direction != null)
				entity.setDirection(entityConfig.direction);
		}

		return world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.IDisposable#dispose()
	 */
	@Override
	public void dispose()
	{
		m_worldScript.onLeave();

		for (Entity e : m_entities)
			e.disassociate();

		for (WorldLayer layer : m_layers)
			layer.dispose();

		m_entities.clear();
		m_layers.clear();
	}

	public void addObserver(IWorldObserver o)
	{
		m_observers.add(o);
	}

	public void removeObserver(IWorldObserver o)
	{
		m_observers.remove(o);
	}
	
	public void addActorRenderFilter(IActorRenderFilter filter)
	{
		m_renderFilters.add(filter);
	}
	
	public void removeActorRenderFilter(IActorRenderFilter filter)
	{
		m_renderFilters.remove(filter);
	}

	public Vector2F translateScreenToWorld(Vector2D location, float fScale)
	{
		return m_worldToScreenMatrix.scale(fScale).inverse().dot(location);
	}

	public Vector2D translateWorldToScreen(Vector2F location, float fScale)
	{
		return m_worldToScreenMatrix.scale(fScale).dot(location).round();
	}

	public Matrix2X2 getPerspectiveMatrix(float fScale)
	{
		return m_worldToScreenMatrix.scale(fScale);
	}

	public int getEntityLayer()
	{
		return m_entityLayer;
	}

	public void setEntityLayer(int layer)
	{
		m_entityLayer = layer;
	}

	public SceneLighting getLighting()
	{
		return m_worldLighting;
	}

	public int getWidth()
	{
		return m_worldWidth;
	}

	public int getHeight()
	{
		return m_worldHeight;
	}

	public int getTileWidth()
	{
		return m_tileWidth;
	}

	public int getTileHeight()
	{
		return m_tileHeight;
	}

	public void pause()
	{
		m_isPaused = true;

		for (Entity e : m_entities)
			e.pause();
	}

	public void resume()
	{
		m_isPaused = false;

		for (Entity e : m_entities)
			e.resume();
	}

	public RouteNode getRouteNode(Vector2D location)
	{
		if (!m_routeNodes.containsKey(location))
			m_routeNodes.put(location, new RouteNode(this, location));

		return m_routeNodes.get(location);
	}

	public TileEffects getTileEffects(Vector2D location)
	{
		TileEffects effects = new TileEffects();

		for (WorldLayer layer : m_layers)
			effects.overlay(layer.getTileEffects(location));

		effects.overlay(m_entityEffectMap.getTileEffects(location));

		return effects;
	}

	public TileEffects[] getTileEffects(ISearchFilter<TileEffects> filter)
	{
		ArrayList<TileEffects> tileEffects = new ArrayList<TileEffects>();

		Rect2D searchBounds = filter.getSearchBounds();

		for (int x = searchBounds.x; x <= searchBounds.x + searchBounds.width; x++)
		{
			for (int y = searchBounds.y; y <= searchBounds.y + searchBounds.height; y++)
			{
				TileEffects effects = getTileEffects(new Vector2D(x, y));

				if (effects != null && filter.shouldInclude(new Vector2F(x, y)) && (effects = filter.filter(effects)) != null)
				{
					tileEffects.add(effects);
				}
			}
		}

		return tileEffects.toArray(new TileEffects[tileEffects.size()]);
	}

	public Entity[] getEntities()
	{
		return m_entities.toArray(new Entity[m_entities.size()]);
	}

	@Nullable
	public Entity getEntity(String name)
	{
		for(Entity e : m_entities)
		{
			if(e.getInstanceName().equals(name))
				return e;
		}
		
		return null;
	}
	
	public Actor[] getActors(ISearchFilter<Actor> filter)
	{
		ArrayList<Actor> found = new ArrayList<Actor>();

		for (Entity entity : m_entities)
		{
			if (entity instanceof Actor)
			{
				Actor actor = (Actor) entity;

				if (filter.shouldInclude(actor.getLocation()))
					found.add(actor);
			}
		}

		return found.toArray(new Actor[found.size()]);
	}

	public WorldLayer[] getLayers()
	{
		return m_layers.toArray(new WorldLayer[m_layers.size()]);
	}

	public void addLayer(WorldLayer layer)
	{
		m_layers.add(layer);
	}

	public void removeLayer(WorldLayer worldLayer)
	{
		m_layers.remove(worldLayer);
	}

	public void addEntity(Entity entity)
	{
		m_additionEntities.add(entity);
	}

	public void removeEntity(Entity entity)
	{
		if (m_additionEntities.contains(entity))
			m_additionEntities.remove(entity);
		else
		{
			m_entities.remove(entity);
			entity.disassociate();
			m_observers.removedEntity(entity);
		}
	}

	public WorldScriptContext getScriptBridge()
	{
		return new WorldScriptContext();
	}

	public Rect2D getBounds()
	{
		return new Rect2D(0, 0, m_worldWidth, m_worldHeight);
	}

	public void update(int delta)
	{
		m_worldTime.add(Calendar.MILLISECOND, (int) (delta * m_fTimeMultiplier));

		m_timeSinceTick += delta;

		if(m_timeSinceTick > WORLD_TICK_INTERVAL)
		{
			m_worldScript.onTick(m_timeSinceTick);
			m_timeSinceTick = 0;
		}
		
		// Entities must be added on update cycles to assure they
		// are not being added to the world in an inapproriate state
		// (i.e. notifying observers of entering world while world is still
		// initializing.)
		for (Entity e : m_additionEntities)
		{
			m_entities.add(e);
			e.associate(this);

			if (m_isPaused)
				e.pause();
			else
				e.resume();

			m_observers.addedEntity(e);
		}

		m_additionEntities.clear();

		m_entityEffectMap.clear();

		for (WorldLayer layer : m_layers)
			layer.update(delta);

		// These have to be separate iterations
		// Otherwise half initialized effect map is used.

		for (Entity entity : m_entities)
			entity.blendEffectMap(m_entityEffectMap);

		for (Entity entity : m_entities)
		{
			// Logic in some entities can result in disassociation and moving of
			// other entities.
			// Thus we must check entity world association before updating it
			if (entity.isAssociated() && entity.getWorld() == this)
				entity.update(delta);
		}
	}

	private void renderQueue(Graphics2D g, int offsetX, int offsetY, float scale)
	{
		for(IActorRenderFilter f : m_renderFilters)
			f.beginBatch(g, scale);
		
		for (Map.Entry<Vector3F, ArrayList<WorldRenderEntry>> entry : m_renderQueue.entrySet())
		{	
			Vector2D renderLocation = translateWorldToScreen(new Vector2F(entry.getKey().x, entry.getKey().y), scale).add(new Vector2D(offsetX, offsetY));

			for (WorldRenderEntry renderable : entry.getValue())
			{
				for(IActorRenderFilter f : m_renderFilters)
					f.begin(renderable);
				
				renderable.graphic.render(g, renderLocation.x, renderLocation.y, scale);

				for(IActorRenderFilter f : m_renderFilters)
					f.end();
			}
		}
		
		for(IActorRenderFilter f : m_renderFilters)
			f.endBatch();
	}
	
	private void clearQueue()
	{
		m_renderQueue.clear();
	}

	protected void enqueueRender(IRenderable renderable, Actor dispatcher, Vector2F location)
	{
		Vector3F location3 = new Vector3F(location, m_renderQueueDepth);

		if (!m_renderQueue.containsKey(location3))
		{
			m_renderQueue.put(location3, new ArrayList<WorldRenderEntry>());
		}

		m_renderQueue.get(location3).add(new WorldRenderEntry(dispatcher, renderable, m_renderQueueDepth));
	}
	
	protected void enqueueRender(IRenderable renderable, Vector2F location)
	{
		enqueueRender(renderable, null, location);
	}
	
	private void setRenderQueueLayerDepth(float fDepth)
	{
		m_renderQueueDepth = fDepth;
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends Actor> T pick(Class<T> clazz, int x, int y, int offsetX, int offsetY, float scale)
	{
		for (Map.Entry<Vector3F, ArrayList<WorldRenderEntry>> entry : m_renderQueue.descendingMap().entrySet())
		{
			Vector2D renderLocation = translateWorldToScreen(new Vector2F(entry.getKey().x, entry.getKey().y), scale).add(new Vector2D(offsetX, offsetY));
			
			Vector2D relativePick = new Vector2D(x - renderLocation.x, y - renderLocation.y);
			
			for (WorldRenderEntry renderable : entry.getValue())
			{
				Actor dispatcher = renderable.dispatcher;
					
				if(dispatcher != null &&
					clazz.isAssignableFrom(dispatcher.getClass()) &&
					dispatcher.testPick(relativePick.x, relativePick.y, scale))
					return (T)dispatcher;
			}
		}
		
		return null;
	}
	
	public void render(Graphics2D g, float fScale, Rectangle viewBounds, int x, int y)
	{
		clearQueue();
		
		Vector2D tlWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x, -viewBounds.y), fScale).round();
		Vector2D trWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x + viewBounds.width, -viewBounds.y), fScale).round();
		Vector2D blWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x, -viewBounds.y + viewBounds.height), fScale).round();
		Vector2D brWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x + viewBounds.width, -viewBounds.y + viewBounds.height), fScale).round();

		Rectangle worldViewBounds = new Rectangle(tlWorldBounds.x, trWorldBounds.y, brWorldBounds.x - tlWorldBounds.x, blWorldBounds.y - trWorldBounds.y);

		for (int i = 0; i < m_layers.size(); i++)
		{
			setRenderQueueLayerDepth(i);
			m_layers.get(i).enqueueRender(worldViewBounds);
		}

		setRenderQueueLayerDepth(m_entityLayer);

		for (Entity entity : m_entities)
		{
			if (entity instanceof Actor)
				((Actor) entity).enqueueRender();
		}

		setRenderQueueLayerDepth(Float.MAX_VALUE);

		if (m_worldLighting.getTargetWidth() != viewBounds.width ||
				m_worldLighting.getTargetHeight() != viewBounds.height)
			
			m_worldLighting.setTargetBounds(viewBounds.width, viewBounds.height);

		m_worldLighting.enqueueRender(this, g.getDeviceConfiguration(), viewBounds, x, y, fScale);

		renderQueue(g, viewBounds.x + x, viewBounds.y + y, fScale);
	}
	
	public interface IActorRenderFilter
	{
		void beginBatch(Graphics2D g, float scale);
		void begin(WorldRenderEntry e);
		void end();
		void endBatch();
	}
	
	private class WorldScriptManager
	{
		@Nullable private Script m_worldScript;

		public WorldScriptManager(Script script)
		{
			m_worldScript = script;
		}

		public WorldScriptManager()
		{
			m_worldScript = null;
		}

		public void onEnter()
		{
			if (m_worldScript == null)
				return;

			try
			{
				m_worldScript.invokeScriptFunction("onEnter");
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error executing world's onEnter script routine." + e.toString());
			} catch (NoSuchMethodException e)
			{
			}
		}

		public void onLeave()
		{
			if (m_worldScript == null)
				return;

			try
			{
				m_worldScript.invokeScriptFunction("onLeave");
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error executing world's onLoaded script routine.");
			} catch (NoSuchMethodException e)
			{
			}
		}

		public void onTick(int delta)
		{
			if (m_worldScript == null)
				return;

			try
			{
				m_worldScript.invokeScriptFunction("onTick", delta);
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error executing world's onTimeTick script routine." + e.toString());
			} catch (NoSuchMethodException e)
			{
			}
		}
	}

	private static class Observers extends StaticSet<IWorldObserver>
	{

		public void addedEntity(Entity e)
		{
			for (IWorldObserver o : this)
				o.addedEntity(e);
		}

		public void removedEntity(Entity e)
		{
			for (IWorldObserver o : this)
				o.removedEntity(e);
		}
	}

	public interface IWorldObserver
	{

		void addedEntity(Entity e);

		void removedEntity(Entity e);
	}
	
	public static class WorldRenderEntry
	{
		private IRenderable graphic;
		
		@Nullable
		private Actor dispatcher;
		
		private float layer;
		
		public WorldRenderEntry(Actor _dispatcher, IRenderable _graphic, float _layer)
		{
			graphic = _graphic;
			dispatcher = _dispatcher;
			layer = _layer;
		}
		
		@Nullable
		public Actor getDispatcher()
		{
			return dispatcher;
		}
		
		public float getLayer()
		{
			return layer;
		}
	}
	
	public class WorldScriptContext
	{
		public EntityBridge<?> createNamedEntity(@Nullable String name, String entityTypeName, String config)
		{
			String instanceName = name == null || name.length() == 0 ? null : name;

			ResourceLibrary resourceLib = Core.getService(ResourceLibrary.class);
			
			Entity entity = resourceLib.createEntity(resourceLib.lookupEntity(entityTypeName), instanceName, config);

			World.this.addEntity(entity);

			return entity.getScriptBridge();
		}

		public EntityBridge<?> createEntity(String className, String config)
		{
			return createNamedEntity(null, className, config);
		}

		public EntityBridge<?> getEntity(String name)
		{
			for(Entity e : m_entities)
			{
				if(e.getInstanceName().equals(name))
					return e.getScriptBridge();
			}
			
			return null;
		}

		public void addEntity(EntityBridge<Entity> entity)
		{
			World.this.addEntity(entity.getEntity());
		}

		public int getMonth()
		{
			return m_worldTime.get(Calendar.DAY_OF_MONTH);
		}

		public int getDay()
		{
			return m_worldTime.get(Calendar.DAY_OF_MONTH);
		}

		public int getHour()
		{
			return m_worldTime.get(Calendar.HOUR_OF_DAY);
		}

		public int getMinute()
		{
			return m_worldTime.get(Calendar.MINUTE);
		}

		public int getSecond()
		{
			return m_worldTime.get(Calendar.SECOND);
		}

		public void setYear(int year)
		{
			m_worldTime.set(Calendar.YEAR, year);
		}

		public void setMonth(int month)
		{
			m_worldTime.set(Calendar.DAY_OF_MONTH, month);
		}

		public void setHour(int hour)
		{
			m_worldTime.set(Calendar.HOUR_OF_DAY, hour);
		}

		public void setDay(int day)
		{
			m_worldTime.set(Calendar.DAY_OF_MONTH, day);
		}

		public void setMinute(int minute)
		{
			m_worldTime.set(Calendar.MINUTE, minute);
		}

		public void setSecond(int second)
		{
			m_worldTime.set(Calendar.SECOND, second);
		}

		public void setTimeMultiplier(float fMultiplier)
		{
			m_fTimeMultiplier = fMultiplier;
		}

		public void setAmbientLight(int r, int g, int b, int a)
		{
			World.this.m_worldLighting.setAmbientLight(new Color((int) Math.min(255, Math.max(0, r)), (int) Math.min(255, Math.max(0, g)), (int) Math.min(255, Math.max(0, b)), (int) Math.min(255, Math.max(0, a))));
		}

		public void pause()
		{
			World.this.pause();
		}

		public void resume()
		{
			World.this.resume();
		}
	}
	
	public static class WorldConfiguration implements ISerializable
	{	
		@Nullable
		public String script;
		
		public int tileWidth;
		public int tileHeight;
		public int worldWidth;
		public int worldHeight;
		public int entityLayer;
		
		public TileDeclaration[] tiles;
		public LayerDeclaration[] layers;
		public EntityDeclaration[] entities;

		public WorldConfiguration() {}
		
		@Override
		public void serialize(IVariable target)
		{
			if(this.script != null)
				target.addChild("script").setValue(this.script);
			
			target.addChild("tileWidth").setValue(this.tileWidth);
			target.addChild("tileHeight").setValue(this.tileHeight);
			target.addChild("worldWidth").setValue(this.worldWidth);
			target.addChild("worldHeight").setValue(this.worldHeight);
			target.addChild("entityLayer").setValue(this.entityLayer);
			target.addChild("tiles").setValue(this.tiles);
			target.addChild("layers").setValue(this.layers);
			target.addChild("entities").setValue(this.entities);
		}

		@Override
		public void deserialize(IVariable source)
		{
			if(source.childExists("script"))
				this.script = source.getChild("script").getValue(String.class);
			
			this.tileWidth = source.getChild("tileWidth").getValue(Integer.class);
			this.tileHeight = source.getChild("tileHeight").getValue(Integer.class);
			this.worldWidth = source.getChild("worldWidth").getValue(Integer.class);
			this.worldHeight = source.getChild("worldHeight").getValue(Integer.class);
			this.entityLayer = source.getChild("entityLayer").getValue(Integer.class);
			this.tiles = source.getChild("tiles").getValues(TileDeclaration[].class);
			this.layers = source.getChild("layers").getValues(LayerDeclaration[].class);
			this.entities = source.getChild("entities").getValues(EntityDeclaration[].class);
		}
		
		public static class TileDeclaration implements ISerializable
		{
			public String sprite;
			public String animation;
			public float visibility;
			public boolean allowRenderSplitting;
			public boolean isTraversable;
			public boolean isStatic;

			public TileDeclaration() { }
			
			@Override
			public void serialize(IVariable target)
			{
				target.addChild("sprite").setValue(this.sprite);
				target.addChild("animation").setValue(this.animation);
				target.addChild("visiblity").setValue(this.visibility);
				target.addChild("allowRenderSplitting").setValue(this.allowRenderSplitting);
				target.addChild("isTraversable").setValue(this.isTraversable);
				target.addChild("isStatic").setValue(this.isStatic);
			}

			@Override
			public void deserialize(IVariable source)
			{
				this.sprite = source.getChild("sprite").getValue(String.class);
				this.animation = source.getChild("animation").getValue(String.class);
				this.visibility = source.getChild("visiblity").getValue(Double.class).floatValue();
				this.allowRenderSplitting = source.getChild("allowRenderSplitting").getValue(Boolean.class);
				this.isTraversable = source.getChild("isTraversable").getValue(Boolean.class);
				this.isStatic = source.getChild("isStatic").getValue(Boolean.class);
			}
		}
		
		public static class LayerDeclaration implements ISerializable
		{
			public int[] tileIndices = new int[0];
			
			public LayerDeclaration() { }

			@Override
			public void serialize(IVariable target)
			{
				target.addChild("indice").setValue(this.tileIndices);
			}

			@Override
			public void deserialize(IVariable source)
			{
				Integer indice[] = source.getChild("indice").getValues(Integer[].class);
				
				this.tileIndices = new int[indice.length];
				
				for(int i = 0; i < indice.length; i++)
					this.tileIndices[i] = indice[i];
			}
		}
		
		public static class EntityDeclaration implements ISerializable
		{
			@Nullable
			public String name;
			
			public String type;
			
			@Nullable
			public Vector2F location;
			
			@Nullable
			public WorldDirection direction;
			
			@Nullable
			public String config;
			
			public EntityDeclaration() {}

			@Override
			public void serialize(IVariable target)
			{
				if(this.name != null && type.length() > 0)
					target.addChild("name").setValue(this.name);
				
				target.addChild("type").setValue("type");
				
				if(this.location != null)
					target.addChild("location").setValue(this.location);
				
				if(this.direction != null)
					target.addChild("direction").setValue(this.direction.ordinal());
				
				if(this.config != null && config.length() > 0)
					target.addChild("config").setValue(this.config);
			}

			@Override
			public void deserialize(IVariable source)
			{
				if(source.childExists("name"))
					this.name = source.getChild("name").getValue(String.class);
				
				type = source.getChild("type").getValue(String.class);
				
				if(source.childExists("location"))
					this.location = source.getChild("location").getValue(Vector2F.class);
				
				if(source.childExists("direction"))
					this.direction = WorldDirection.values()[source.getChild("direction").getValue(Integer.class)];
				
				if(source.childExists("config"))
					this.config = source.getChild("config").getValue(String.class);
			}
		}
	}
}
