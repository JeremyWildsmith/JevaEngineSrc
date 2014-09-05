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
package io.github.jevaengine.client;

import io.github.jevaengine.Core;
import io.github.jevaengine.FutureResult;
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
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.SynchronousExecutor.ISynchronousTask;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.DefaultWorldFactory;
import io.github.jevaengine.world.DefaultWorldFactory.WorldConfiguration;
import io.github.jevaengine.world.entity.DefaultEntity;

import java.util.ArrayList;

@SharedClass(name = "World", policy = SharePolicy.ClientR)
public final class ClientWorld extends SharedEntity
{
	private static final int SYNC_INTERVAL = 200;

	@Nullable
	private NetWorldIdentifier m_worldName;
	
	@Nullable
	private World m_world;

	private boolean m_isValid = false;
	
	private ArrayList<IInitializationMonitor<ClientWorld>> m_monitors = new ArrayList<>();
	private ArrayList<SharedEntity> m_exclusiveSharedEntities = new ArrayList<>();
	
	public ClientWorld()
	{
		super(SYNC_INTERVAL);
		
		enqueueLogicTask(new ISynchronousTask() {

			@Override
			public boolean run()
			{
				send(Signal.InitializeRequest);
				return true;
			}
		});
	}
	
	public void monitorInitialization(IInitializationMonitor<ClientWorld> monitor)
	{
		if(isReady())
			monitor.completed(new FutureResult<ClientWorld>(this));
		else
			m_monitors.add(monitor);
	}
	
	public boolean isValid()
	{
		return m_isValid;
	}
	
	public boolean isReady()
	{
		return m_world != null;
	}

	@Nullable
	public World getWorld()
	{
		return m_world;
	}
	
	@Nullable
	public NetWorldIdentifier getWorldName()
	{
		return m_worldName;
	}

	@Override
	protected void doSynchronization() throws InvalidMessageException
	{
		for(SharedEntity e : m_exclusiveSharedEntities)
			e.synchronize();
	}
	
	@Override
	public void doLogic(int deltaTime)
	{
		if(m_world != null)
			m_world.update(deltaTime);
		
		for(SharedEntity e : m_exclusiveSharedEntities)
			e.update(deltaTime);
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object message) throws InvalidMessageException
	{
		if (message instanceof InitializationArguments)
		{
			m_worldName = ((InitializationArguments) message).getWorldName();
			m_isValid = true;
			
			final String map = m_worldName.getFormalName();
			
			new DefaultWorldFactory().createAsynchronously(Core.getService(ResourceLibrary.class).openConfiguration(map).getValue(WorldConfiguration.class), new IInitializationMonitor<World>() {
				@Override
				public void statusChanged(final float progress, final String status)
				{
					synchronized(m_monitors)
					{
						for(IInitializationMonitor<ClientWorld> m : m_monitors)
							m.statusChanged(progress, status);
					}
				}

				@Override
				public void completed(final FutureResult<World> item)
				{
					try
					{
						m_world = item.get();
					}catch(Exception e)
					{
						throw new RuntimeException(e);
					}
					
					synchronized(m_monitors)
					{
						for(IInitializationMonitor<ClientWorld> m : m_monitors)
							m.completed(new FutureResult<ClientWorld>(ClientWorld.this));
								
						m_monitors.clear();	
					}
				}
			});
		}

		return true;
	}

	@Override
	protected void onEntityShared(SharedEntity sharedEntity)
	{
		if(!(sharedEntity instanceof ClientEntity))
			return;
		
		
		@SuppressWarnings("unchecked") //Generic type argument must extend Entity as per class declaration.
		final ClientEntity<DefaultEntity> clientWorldEntity = (ClientEntity<DefaultEntity>)sharedEntity;
		
		//Since this method is invoked on a synchronous context (i.e, via the same thread as doLogic, doSynchronization
		//onMessageRecieved (along with all synchronous tasks) it is safe assume we are not creating a race condition as
		//clientWorldEntity will only become ready synchronously.
		if(!clientWorldEntity.isReady())
		{
			clientWorldEntity.monitorInitialization(new IInitializationMonitor<DefaultEntity>() {
				@Override
				public void statusChanged(float progress, String status) { }

				@Override
				public void completed(final FutureResult<DefaultEntity> item)
				{
					enqueueLogicTask(new ISynchronousTask() {

						@Override
						public boolean run()
						{
							try
							{
								if(isSharing(clientWorldEntity))
								{
									if(m_world == null) // If we're still waiting for the world to load, requeue the task.
										return false;
									
									m_world.addEntity(item.get());
								}else
									return true;
							}
							catch(Exception e)
							{
								throw new RuntimeException(e);
							}
							
							return true;
						}
					});
				}
			});
		}
		else
			m_world.addEntity(clientWorldEntity.getEntity());
		
		m_exclusiveSharedEntities.add(sharedEntity);
	}
	
	@Override
	protected void onEntityUnshared(SharedEntity sharedEntity)
	{
		if(!(sharedEntity instanceof ClientEntity))
			return;
		
		ClientEntity<?> clientWorldEntity = (ClientEntity<?>)sharedEntity;
		
		if(clientWorldEntity.isReady())
			m_world.removeEntity(clientWorldEntity.getEntity());
		
		m_exclusiveSharedEntities.remove(sharedEntity);
	}
}
