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
package io.github.jevaengine.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.netcommon.world.InitializationArguments;
import io.github.jevaengine.netcommon.world.NetWorldIdentifier;
import io.github.jevaengine.netcommon.world.Signal;
import io.github.jevaengine.server.library.IServerLibrary;
import io.github.jevaengine.server.library.IServerLibrary.IServerEntityWrapFactory;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.ThreadSafe;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.IWorldObserver;
import io.github.jevaengine.world.DefaultWorldFactory;
import io.github.jevaengine.world.DefaultWorldFactory.WorldConfiguration;
import io.github.jevaengine.world.entity.DefaultEntity;
import io.github.jevaengine.world.entity.DefaultEntity.DefaultEntityBridge;

import java.util.concurrent.ConcurrentHashMap;

@SharedClass(name = "World", policy = SharePolicy.ClientR)
public final class ServerWorld extends SharedEntity implements IDisposable
{
	private static final int SYNC_INTERVAL = 200;

	//Concurrent as entities can be looked up in a thread safe manner.
	private ConcurrentHashMap<DefaultEntity, ServerEntity<? extends DefaultEntity>> m_serverEntities = new ConcurrentHashMap<>();
	
	private final NetWorldIdentifier m_worldName;

	private World m_world;
	
	private ServerWorld(String worldName, World world)
	{
		super(SYNC_INTERVAL);
		
		m_worldName = new NetWorldIdentifier(worldName);
		m_world = world;
		m_world.addObserver(new WorldObserver());
	}

	public static void create(final NetWorldIdentifier worldName, final IInitializationMonitor<ServerWorld> monitor)
	{
		WorldConfiguration config = Core.getService(ResourceLibrary.class).openConfiguration(worldName.getFormalName()).getValue(WorldConfiguration.class);
		
		new DefaultWorldFactory().create(config, new IInitializationMonitor<World>() {
			@Override
			public void statusChanged(float progress, String status)
			{
				monitor.statusChanged(progress, status);
			}

			@Override
			public void completed(FutureResult<World> item)
			{
				try
				{
					World world = item.get();
					monitor.completed(new FutureResult<ServerWorld>(new ServerWorld(worldName.getFormalName(), world)));
				}catch(Exception e)
				{
					monitor.completed(new FutureResult<ServerWorld>(e));
				}
			}
		});
	}
	
	@Override
	public void dispose()
	{
		if(m_world != null)
			m_world.dispose();
	}
	
	@Override
	protected void doSynchronization() throws InvalidMessageException
	{
		for(ServerEntity<? extends DefaultEntity> e : m_serverEntities.values())
			e.synchronize();
	}

	@Override
	protected void doLogic(int deltaTime)
	{
		if(m_world != null)
			m_world.update(deltaTime);
		
		for(ServerEntity<? extends DefaultEntity> e : m_serverEntities.values())
			e.update(deltaTime);
	}

	public ServerWorldBridge getBridge()
	{
		return new ServerWorldBridge();
	}
	
	@ThreadSafe
	public NetWorldIdentifier getName()
	{
		return m_worldName;
	}
	
	@Nullable
	@ThreadSafe
	protected World getWorld()
	{
		return m_world;
	}
	
	protected boolean isReady()
	{
		return m_world != null;
	}
	
	@ThreadSafe
	@Nullable
	public ServerEntity<? extends DefaultEntity> lookupEntity(DefaultEntity entity)
	{
		return m_serverEntities.get(entity);
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if (recv instanceof Signal && (Signal)recv == Signal.InitializeRequest)
		{
			send(sender, new InitializationArguments(m_worldName));
		} else
			throw new InvalidMessageException(sender, recv, "Unrecognized message recieved from server");

		return true;
	}
	
	private class WorldObserver implements IWorldObserver
	{
		@Override
		public void addedEntity(DefaultEntity entity)
		{
			IServerEntityWrapFactory factory = Core.getService(IServerLibrary.class).getServerEntityWrapFactory(entity.getClass());
			
			if(factory != null)
			{
				ServerEntity<? extends DefaultEntity> serverEntity = factory.wrap(entity);
				m_serverEntities.put(entity, serverEntity);
				share(serverEntity);
			}
		}

		@Override
		public void removedEntity(DefaultEntity entity)
		{
			ServerEntity<?> serverEntity = m_serverEntities.get(entity);
			
			if(serverEntity != null)
			{
				m_serverEntities.remove(entity);
				unshare(serverEntity);
				serverEntity.dispose();
			}
		}
	}
	
	public class ServerWorldBridge
	{
		public ServerEntity<?>.ServerEntityBridge lookupServerEntity(DefaultEntityBridge<?> entityBridge)
		{
			ServerEntity<?> entity = ServerWorld.this.lookupEntity(entityBridge.getEntity());
			
			return entity == null ? null : entity.getBridge();
		}
	}
}
