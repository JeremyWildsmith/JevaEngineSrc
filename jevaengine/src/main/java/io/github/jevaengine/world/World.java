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

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.script.IFunction;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.NullFunctionFactory;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.ScriptHiddenMember;
import io.github.jevaengine.script.UnrecognizedFunctionException;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.util.SynchronousExecutor;
import io.github.jevaengine.util.SynchronousExecutor.ISynchronousTask;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.SceneGraph.EntityContainerObserver;
import io.github.jevaengine.world.SceneGraph.EntitySet;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.IEntity.IEntityBridge;
import io.github.jevaengine.world.entity.IEntityFactory.EntityConstructionException;
import io.github.jevaengine.world.entity.IParallelEntityFactory;
import io.github.jevaengine.world.physics.IPhysicsWorld;
import io.github.jevaengine.world.scene.ISceneBuffer;
import io.github.jevaengine.world.search.ISearchFilter;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class World implements IDisposable
{
	private final Logger m_logger = LoggerFactory.getLogger(World.class);
	private final Observers m_observers = new Observers();

	private SceneGraph m_entityContainer;
	private Rect2D m_worldBounds;
	
	private WorldBridgeNotifier m_script;

	private SynchronousExecutor m_syncExecuter = new SynchronousExecutor();

	private final IPhysicsWorld m_physicsWorld;
	private final IParallelEntityFactory m_entityFactory;
	
	public World(int worldWidth, int worldHeight, IPhysicsWorld physicsWorld, IParallelEntityFactory entityFactory, @Nullable IScriptFactory scriptFactory, @Nullable String worldScript)
	{
		m_entityFactory = entityFactory;
		m_physicsWorld = physicsWorld;
		m_worldBounds = new Rect2D(worldWidth, worldHeight);
		
		m_entityContainer = new SceneGraph(physicsWorld);
		m_entityContainer.addObserver(new WorldEntityObserver());
		
		if (worldScript != null && scriptFactory != null)
			m_script = new WorldBridgeNotifier(scriptFactory, worldScript);
		else
			m_script = new WorldBridgeNotifier();
	}
	
	public World(int worldWidth, int worldHeight, IPhysicsWorld physicsWorld, IParallelEntityFactory entityFactory)
	{
		this(worldWidth, worldHeight, physicsWorld, entityFactory, null, null);
	}
	
	@Override
	public void dispose()
	{
		m_entityContainer.dispose();
	}

	public IPhysicsWorld getPhysicsWorld()
	{
		return m_physicsWorld;
	}
	
	public void addObserver(IWorldObserver o)
	{
		m_observers.add(o);
	}

	public void removeObserver(IWorldObserver o)
	{
		m_observers.remove(o);
	}
	
	public Rect2D getBounds()
	{
		return new Rect2D(m_worldBounds);
	}

	public TileEffects getTileEffects(Vector2D location)
	{
		if(location.x >= m_worldBounds.width || location.y >= m_worldBounds.height || location.x < 0 || location.y < 0)
			return new TileEffects(false);
		else
			return m_entityContainer.getTileEffects(location);
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

	public void addEntity(IEntity entity)
	{
		entity.associate(this);
		m_entityContainer.add(entity);
	}

	public void removeEntity(IEntity entity)
	{
		if(entity.getWorld() == this)
			entity.disassociate();
		
		m_entityContainer.remove(entity);
	}
	
	public EntitySet getEntities()
	{
		return m_entityContainer.getEntities();
	}
	
	public WorldBridge getScriptBridge()
	{
		return m_script.getScriptBridge();
	}

	public void update(int delta)
	{
		m_syncExecuter.execute();
		m_entityContainer.update(delta);
		
		//It is important that the physics world be updated after the entities have been updated.
		//The forces to be applied this cycle may be relative to the delta time elapsed since last cycle.
		m_physicsWorld.update(delta);
	}
	
	public void fillScene(ISceneBuffer sceneBuffer, Rect2F region)
	{
		m_entityContainer.enqueueRender(sceneBuffer, region);
	}
	
	private class WorldEntityObserver implements EntityContainerObserver
	{
		@Override
		public void addedEntity(IEntity e)
		{
			m_observers.addedEntity(e);
		}

		@Override
		public void removedEntity(IEntity e)
		{
			m_observers.removedEntity(e);
		}
	}
	
	private class WorldBridgeNotifier
	{		
		private WorldBridge m_bridge;
		
		public WorldBridgeNotifier()
		{
			m_bridge = new WorldBridge(new NullFunctionFactory());
		}
		
		public WorldBridge getScriptBridge()
		{
			return m_bridge;
		}

		public WorldBridgeNotifier(IScriptFactory scriptFactory, String script)
		{
			m_bridge = new WorldBridge(scriptFactory.getFunctionFactory());
			
			try
			{
				scriptFactory.create(m_bridge, script);
			} catch(AssetConstructionException e)
			{
				m_logger.error("Error instantiating world script", e);
			}
		}
	}

	private static class Observers extends StaticSet<IWorldObserver>
	{
		public void addedEntity(IEntity e)
		{
			for (IWorldObserver o : this)
				o.addedEntity(e);
		}

		public void removedEntity(IEntity e)
		{
			for (IWorldObserver o : this)
				o.removedEntity(e);
		}
	}

	public interface IWorldObserver
	{
		void addedEntity(IEntity e);
		void removedEntity(IEntity e);
	}
	
	public final class WorldBridge
	{
		private final IFunctionFactory m_functionFactory;
		
		public final ScriptEvent onTick;
		
		private Logger m_logger = LoggerFactory.getLogger(WorldBridge.class);
		
		private WorldBridge(IFunctionFactory functionFactory)
		{
			onTick = new ScriptEvent(functionFactory);
			m_functionFactory = functionFactory;
		}
		
		@ScriptHiddenMember
		public World getWorld()
		{
			return World.this;
		}
		
		@Nullable
		private IFunction wrapFunction(Object function)
		{
			if(function == null)
				return null;
			
			try
			{
				return m_functionFactory.wrap(function);
			} catch (UnrecognizedFunctionException e) {
				m_logger.error("Could not wrap function, replacing with null behavior.");
				return null;
			}
		}
		
		public void createNamedEntity(@Nullable String name, String entityTypeName, String config, @Nullable final Object rawSuccessCallback)
		{
			String instanceName = name == null || name.length() == 0 ? null : name;

			final IFunction successCallback = wrapFunction(rawSuccessCallback);

			m_entityFactory.create(entityTypeName, instanceName, config, new IInitializationMonitor<IEntity, EntityConstructionException>() {

				@Override
				public void statusChanged(float progress, String status) { }

				@Override
				public void completed(final FutureResult<IEntity, EntityConstructionException> item)
				{
					m_syncExecuter.enqueue(new ISynchronousTask() {
						@Override
						public boolean run()
						{
							try
							{
								final IEntity entity = item.get();
								World.this.addEntity(entity);
								
								if(successCallback != null)
									successCallback.call(entity.getBridge());
							}catch(EntityConstructionException e)
							{
								m_logger.error("Unable to construct entity requested by script:", e);
							} catch (ScriptExecuteException e)
							{
								m_logger.error("Error invoking entity construction success callback", e);
							}
							
							return true;
						}
					});
				}
			});
		}
		
		public void createNamedEntity(@Nullable String name, String entityTypeName, String config)
		{
			createNamedEntity(name, entityTypeName, config, null);
		}
		
		public void createEntity(String className, String config, @Nullable Object successCallback)
		{
			createNamedEntity(null, className, config, successCallback);
		}
		
		public void createEntity(String className, String config)
		{
			createEntity(className, config, null);
		}

		public IEntityBridge getEntity(String name)
		{
			IEntity e = World.this.getEntities().getByName(name);
			
			return e.getBridge();
		}

		public void addEntity(IEntityBridge entity)
		{
			World.this.addEntity(entity.getEntity());
		}
		
		public void removeEntity(IEntityBridge entity)
		{
			World.this.removeEntity(entity.getEntity());
		}
	}
}
