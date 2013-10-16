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
package jeva.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.script.ScriptException;

import proguard.annotation.KeepClassMemberNames;
import jeva.Core;
import jeva.CoreScriptException;
import jeva.IDisposable;
import jeva.IResourceLibrary;
import jeva.Script;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.config.VariableValue;
import jeva.game.ResourceLoadingException;
import jeva.graphics.IRenderable;
import jeva.graphics.Sprite;
import jeva.math.Matrix2X2;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.math.Vector3F;
import jeva.util.StaticSet;
import jeva.world.EffectMap.TileEffects;
import jeva.world.Entity.EntityBridge;


public class World extends Variable implements IDisposable
{

	/** The Constant WORLD_TICK_INTERVAL. */
	private static final int WORLD_TICK_INTERVAL = 3000;

	/** The Constant WORLD_CULLING_EXCCESS. */
	private static final int WORLD_CULLING_EXCCESS = 6;

	/** The m_observers. */
	private final Observers m_observers = new Observers();

	// Read comments in update route for notes on m_additionEntities member.
	/** The m_entity library. */
	private IEntityLibrary m_entityLibrary;

	/** The m_addition entities. */
	private ArrayList<Entity> m_additionEntities = new ArrayList<Entity>();

	/** The m_entities. */
	private StaticSet<Entity> m_entities;

	/** The m_entity layer. */
	private int m_entityLayer;

	/** The m_render queue. */
	private TreeMap<Vector3F, ArrayList<IRenderable>> m_renderQueue;

	/** The m_f render queue depth. */
	private float m_fRenderQueueDepth;

	/** The m_route nodes. */
	private HashMap<Vector2D, RouteNode> m_routeNodes;

	/** The m_entity effect map. */
	private EffectMap m_entityEffectMap;

	/** The m_layers. */
	private ArrayList<WorldLayer> m_layers;

	/** The m_world lighting. */
	private SceneLighting m_worldLighting;

	/** The m_world width. */
	private int m_worldWidth;

	/** The m_world height. */
	private int m_worldHeight;

	/** The m_tile width. */
	private int m_tileWidth;

	/** The m_tile height. */
	private int m_tileHeight;

	/** The m_world to screen matrix. */
	private final Matrix2X2 m_worldToScreenMatrix;

	/** The m_world script. */
	private WorldScriptManager m_worldScript;

	/** The m_is paused. */
	private boolean m_isPaused = false;

	/** The m_world time. */
	private Calendar m_worldTime;

	/** The m_f time multiplier. */
	private float m_fTimeMultiplier;

	/** The m_time since tick. */
	private int m_timeSinceTick;

	
	public World(IEntityLibrary library, int width, int height, int tileWidth, int tileHeight, int entityLayer, String worldScript)
	{
		m_entityLibrary = library;

		m_layers = new ArrayList<WorldLayer>();
		m_entities = new StaticSet<Entity>();

		m_renderQueue = new TreeMap<Vector3F, ArrayList<IRenderable>>();
		m_fRenderQueueDepth = 0.0F;

		m_routeNodes = new HashMap<Vector2D, RouteNode>();

		m_entityEffectMap = new EffectMap(new Rectangle(0, 0, width, height));

		m_worldWidth = width;
		m_worldHeight = height;
		m_tileWidth = tileWidth;
		m_tileHeight = tileHeight;

		m_entityLayer = entityLayer;

		m_worldToScreenMatrix = new Matrix2X2(tileWidth / 2.0F, -tileWidth / 2.0F, tileHeight / 2.0F, tileHeight / 2.0F);

		m_worldLighting = new SceneLighting();
		m_worldLighting.associate(this);

		m_worldTime = Calendar.getInstance();
		m_fTimeMultiplier = 1.0F;
		m_timeSinceTick = 0;

		if (worldScript.length() > 0)
			m_worldScript = new WorldScriptManager(worldScript);
		else
			m_worldScript = new WorldScriptManager();

		m_worldScript.onEnter();
	}

	
	public World(IEntityLibrary library, int width, int height, int tileWidth, int tileHeight, int entityLayerDepth)
	{
		this(library, width, height, tileWidth, tileHeight, entityLayerDepth, "");
	}

	
	private static void initEntityProperties(Entity entity, Variable srcProperty, String parentName)
	{
		if (!srcProperty.getValue().getString().isEmpty())
		{
			entity.setVariable(parentName, srcProperty.getValue());
		}

		for (Variable var : srcProperty)
		{
			initEntityProperties(entity, var, parentName + Variable.NAME_SPLIT + var.getName());
		}
	}

	
	private static void initEntityProperties(Entity entity, Variable srcProperty)
	{
		initEntityProperties(entity, srcProperty, srcProperty.getName());
	}

	
	public static World create(IEntityLibrary library, Variable source) throws ResourceLoadingException
	{
		String worldScript = new String();

		if (source.variableExists("script"))
		{
			String scriptName = source.getVariable("script").getValue().getString();

			worldScript = Core.getService(IResourceLibrary.class).openResourceContents(scriptName);
		}

		World world = new World(library, source.getVariable("width").getValue().getInt(), source.getVariable("height").getValue().getInt(), source.getVariable("tileWidth").getValue().getInt(), source.getVariable("tileHeight").getValue().getInt(), source.getVariable("entityLayer").getValue().getInt(), worldScript);

		if (source.variableExists("entity"))
		{
			for (Variable entityVar : source.getVariable("entity"))
			{
				Entity entity = world.getEntityLibrary().createEntity(entityVar.getValue().getObjectName(), entityVar.getName(), Arrays.asList(entityVar.getValue().getObjectArguments()));

				world.addEntity(entity);

				// Assign post-init assignments
				if (source.variableExists(entity.getName()))
				{
					for (Variable varProperty : source.getVariable(entity.getName()))
					{
						initEntityProperties(entity, varProperty);
					}
				}
			}
		}

		// Construct Map Layers
		Variable[] tileDeclarations = source.getVariable("tile").getVariableArray();

		Variable[] layerDeclarations = source.getVariable("layer").getVariableArray();

		for (int i = 0; i < layerDeclarations.length; i++)
		{
			WorldLayer worldLayer = new WorldLayer();

			Integer[] tileDeclIndices = layerDeclarations[i].getValue().getIntArray();

			for (int tileIndex = 0; tileIndex < tileDeclIndices.length; tileIndex++)
			{
				if (tileDeclIndices[tileIndex] < 0)
					continue;

				if (tileDeclIndices[tileIndex] >= tileDeclarations.length)
					throw new ResourceLoadingException("Undeclared Tile Class Index Used");

				VariableValue[] arguments = tileDeclarations[tileDeclIndices[tileIndex]].getValue().getObjectArguments();

				if (arguments.length < 6)
					throw new ResourceLoadingException("Illegal number of arguments for tile.");

				boolean enableSplitting = false;

				if (arguments.length >= 7)
					enableSplitting = arguments[6].getBoolean();

				Tile tile = new Tile(Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(arguments[0].getString()))), WorldDirection.values()[arguments[1].getInt()], arguments[2].getString(), arguments[3].getBoolean(), enableSplitting, arguments[5].getFloat());

				tile.associate(world);
				// Location of tile must be set _BEFORE_ adding it to map layer
				// so map layer can properly place the tile in the proper sector
				// for optimizations.
				tile.setLocation(new Vector2F(tileIndex % world.m_worldWidth, (float) Math.floor(tileIndex / world.m_worldWidth)));

				if (arguments[4].getBoolean()) // Is Static
					worldLayer.addStaticTile(tile);
				else
					worldLayer.addDynamicTile(tile);
			}

			world.m_layers.add(worldLayer);
		}

		return world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.IDisposable#dispose()
	 */
	public void dispose()
	{
		m_worldScript.onLeave();

		for (Entity e : m_entities)
			e.disassociate();

		for (WorldLayer layer : m_layers)
			layer.dispose();

		m_entities.clear();
		m_layers.clear();
		m_worldLighting.disassociate();
	}

	
	public void addObserver(IWorldObserver o)
	{
		m_observers.add(o);
	}

	
	public void removeObserver(IWorldObserver o)
	{
		m_observers.remove(o);
	}

	
	public Vector2D translateScreenToWorld(Vector2D location, float fScale)
	{
		return m_worldToScreenMatrix.scale(fScale).inverse().dot(location).round();
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

	
	public IEntityLibrary getEntityLibrary()
	{
		return m_entityLibrary;
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

		Rectangle searchBounds = filter.getSearchBounds();

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

	
	public Rectangle getMapBounds()
	{
		return new Rectangle(0, 0, m_worldWidth, m_worldHeight);
	}

	
	public void update(int delta)
	{
		m_worldTime.add(Calendar.MILLISECOND, (int) (delta * m_fTimeMultiplier));

		m_timeSinceTick += delta * m_fTimeMultiplier;

		for (; m_timeSinceTick > WORLD_TICK_INTERVAL; m_timeSinceTick -= WORLD_TICK_INTERVAL)
			m_worldScript.onTick();

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

	
	private void renderQueue(Graphics2D g, int offsetX, int offsetY, float fScale)
	{
		for (Map.Entry<Vector3F, ArrayList<IRenderable>> entry : m_renderQueue.entrySet())
		{
			Vector2D renderLocation = translateWorldToScreen(new Vector2F(entry.getKey().x, entry.getKey().y), fScale).add(new Vector2D(offsetX, offsetY));

			for (IRenderable renderable : entry.getValue())
			{
				renderable.render(g, renderLocation.x, renderLocation.y, fScale);
			}
		}

		m_renderQueue.clear();

	}

	
	protected void enqueueRender(IRenderable renderable, Vector2F location)
	{
		Vector3F location3 = new Vector3F(location, m_fRenderQueueDepth);

		if (!m_renderQueue.containsKey(location3))
		{
			m_renderQueue.put(location3, new ArrayList<IRenderable>());
		}

		m_renderQueue.get(location3).add(renderable);
	}

	
	private void setRenderQueueLayerDepth(float fDepth)
	{
		m_fRenderQueueDepth = fDepth;
	}

	
	public void render(Graphics2D g, float fScale, Rectangle viewBounds)
	{
		Vector2D tlWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x, -viewBounds.y), fScale);
		Vector2D trWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x + viewBounds.width, -viewBounds.y), fScale);
		Vector2D blWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x, -viewBounds.y + viewBounds.height), fScale);
		Vector2D brWorldBounds = translateScreenToWorld(new Vector2D(-viewBounds.x + viewBounds.width, -viewBounds.y + viewBounds.height), fScale);

		Rectangle worldViewBounds = new Rectangle(tlWorldBounds.x, trWorldBounds.y, brWorldBounds.x - tlWorldBounds.x, blWorldBounds.y - trWorldBounds.y);

		worldViewBounds.x -= WORLD_CULLING_EXCCESS;
		worldViewBounds.y -= WORLD_CULLING_EXCCESS;
		worldViewBounds.width += WORLD_CULLING_EXCCESS * 2;
		worldViewBounds.height += WORLD_CULLING_EXCCESS * 2;
		
		for(int i = 0; i < m_layers.size(); i++)
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

		if (m_worldLighting.getTargetWidth() != viewBounds.width || m_worldLighting.getTargetHeight() != viewBounds.height)
			m_worldLighting.setTargetBounds(viewBounds.width, viewBounds.height);

		m_worldLighting.enqueueRender(g.getDeviceConfiguration(), worldViewBounds, m_worldToScreenMatrix, viewBounds.x, viewBounds.y, fScale);

		renderQueue(g, viewBounds.x, viewBounds.y, fScale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#getChildren()
	 */
	@Override
	protected Variable[] getChildren()
	{
		return m_entities.toArray(new Entity[m_entities.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#setChild(java.lang.String,
	 * jeva.config.VariableValue)
	 */
	@Override
	protected Variable setChild(String name, VariableValue value)
	{
		Variable child = getChild(name);
		child.setValue(value);

		return child;
	}

	
	private class WorldScriptManager
	{

		/** The m_world script. */
		private Script m_worldScript;

		
		public WorldScriptManager(String script)
		{
			m_worldScript = new Script();
			m_worldScript.setScript(script, new WorldScriptContext());
		}

		
		public WorldScriptManager()
		{
			m_worldScript = new Script();
		}

		
		public void onEnter()
		{
			if (!m_worldScript.isReady())
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
			if (!m_worldScript.isReady())
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

		
		public void onTick()
		{
			if (!m_worldScript.isReady())
				return;

			try
			{
				m_worldScript.invokeScriptFunction("onTick");
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

	
	@KeepClassMemberNames
	public class WorldScriptContext
	{

		
		public EntityBridge<?> createNamedEntity(String name, String className, String... args)
		{
			ArrayList<VariableValue> argVars = new ArrayList<VariableValue>();

			for (String s : args)
				argVars.add(new VariableValue(s));

			Entity entity;

			if (name == null || name.isEmpty())
				entity = getEntityLibrary().createEntity(className, null, argVars);
			else
				entity = getEntityLibrary().createEntity(className, name, argVars);

			World.this.addEntity(entity);

			return entity.getScriptBridge();
		}

		
		public EntityBridge<?> createEntity(String className, String... args)
		{
			return createNamedEntity(null, className, args);
		}

		
		public EntityBridge<?> getEntity(String name)
		{
			if (variableExists(name))
				return ((Entity) getVariable(name)).getScriptBridge();

			return null;
		}

		
		public void addEntity(EntityBridge<Entity> entity)
		{
			World.this.addEntity(entity.getMe());
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
}
