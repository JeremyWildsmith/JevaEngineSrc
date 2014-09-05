package io.github.jevaengine.server;

import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.world.NetWorldIdentifier;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.World;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerWorldPool implements IServerWorldLookup
{
	private Map<NetWorldIdentifier, ServerWorld> m_worlds = new ConcurrentHashMap<>();
	private Map<NetWorldIdentifier, GetWorldRequest> m_getWorldRequests = new ConcurrentHashMap<>();
	
	//To prevent this method from executing concurrently.
	public synchronized void getWorld(NetWorldIdentifier name, final IInitializationMonitor<ServerWorld> monitor)
	{
		ServerWorld world = lookupServerWorld(name);
		
		if(world != null)
			monitor.completed(new FutureResult<ServerWorld>(world));
		else
		{
			if(!m_getWorldRequests.containsKey(name))
			{
				GetWorldRequest worldRequest = new GetWorldRequest(name);
				worldRequest.addMonitor(monitor);
				
				m_getWorldRequests.put(name, worldRequest);
				ServerWorld.create(name, worldRequest);
			}else
				m_getWorldRequests.get(name).addMonitor(monitor);
		}
	}

	@Override
	@Nullable
	public ServerWorld lookupServerWorld(NetWorldIdentifier worldName)
	{
		return m_worlds.get(worldName);
	}

	@Override
	@Nullable
	public ServerWorld lookupServerWorld(World world)
	{
		for (ServerWorld serverWorld : m_worlds.values())
		{
			if (serverWorld.getWorld() == world)
				return serverWorld;
		}

		return null;
	}
	
	public void update(int deltaTime, IInvalidMessageHandler handler)
	{
		for (ServerWorld serverWorld : m_worlds.values())
		{
			serverWorld.update(deltaTime);
			
			try
			{
				serverWorld.synchronize();
			} catch (InvalidMessageException e)
			{
				handler.handle(e);
			}
		}
	}
	
	private class GetWorldRequest implements IInitializationMonitor<ServerWorld>
	{
		private ArrayList<IInitializationMonitor<ServerWorld>> m_monitors = new ArrayList<>();
		
		private NetWorldIdentifier m_worldName;
		
		public GetWorldRequest(NetWorldIdentifier worldName)
		{
			m_worldName = worldName;
		}
		
		public void addMonitor(IInitializationMonitor<ServerWorld> monitor)
		{
			m_monitors.add(monitor);
		}

		@Override
		public void statusChanged(float progress, String status)
		{
			for(IInitializationMonitor<ServerWorld> m : m_monitors)
				m.statusChanged(progress, status);
		}

		@Override
		public void completed(FutureResult<ServerWorld> item)
		{
			try
			{
				m_worlds.put(m_worldName, item.get());
			}catch(Exception e)
			{
				throw new RuntimeException(e);
			}
			
			for(IInitializationMonitor<ServerWorld> m : m_monitors)
				m.completed(item);
			
			m_getWorldRequests.remove(m_worldName);
		}
	}
}
