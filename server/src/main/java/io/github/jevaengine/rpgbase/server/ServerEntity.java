package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.rpgbase.netcommon.NetEntity.IEntityVisitor;
import io.github.jevaengine.rpgbase.netcommon.NetEntity.InitializeFlags;
import io.github.jevaengine.rpgbase.netcommon.NetEntity.InitializeRequest;
import io.github.jevaengine.rpgbase.netcommon.NetEntity.InitializeEntity;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.Entity.IEntityObserver;

public abstract class ServerEntity<T extends Entity> extends SharedEntity
{
	private static final int SYNC_INTERVAL = 50;
	
	private @Nullable ServerCommunicator m_owner;
	
	private String m_className;
	private @Nullable String m_configuration;
	private int m_tickCount = 0;
	
	private T m_entity;
	
	public ServerEntity(T entity, @Nullable String configuration, @Nullable ServerCommunicator owner)
	{
		m_configuration = configuration;
		m_owner = owner;
		m_entity = entity;
		m_entity.addObserver(new ServerEntityObserver());
		
		m_className = Core.getService(ResourceLibrary.class).lookupEntity(entity.getClass());
	}
	
	public ServerEntity(T entity)
	{
		this(entity, null, null);
	}
	
	public final ServerCommunicator getOwner()
	{
		return m_owner;
	}
	
	public final T getEntity()
	{
		return m_entity;
	}
	
	public final void update(int deltaTime)
	{
		m_tickCount += deltaTime;

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
	}
	
	@Override
	protected final boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		ServerCommunicator communicator = (ServerCommunicator) sender;
		
		if(recv instanceof IEntityVisitor)
		{
			IEntityVisitor visitor = (IEntityVisitor)recv;
			
			if ((!visitor.requiresOwnership() || sender == m_owner) && !visitor.isServerDispatchOnly())
			{
				try
				{
					visitor.visit(sender, getEntity(), true);
				} catch (InvalidMessageException e)
				{
					communicator.disconnect("Visitor synchronization error: " + e.toString());
				}
			}
			else
				communicator.disconnect("Attempted to mutate entity without sufficient access.");
			
		}else if(recv instanceof InitializeRequest)
		{
			dispatchInitialization(sender);
		}else
			throw new InvalidMessageException(sender, recv, "Unrecognized message");

		return true;
	}
	
	private void dispatchInitialization(Communicator sender)
	{
		send(sender, new InitializeEntity(m_className, m_configuration, getEntity().getInstanceName(), m_owner == sender));
		send(sender, new InitializeFlags(getEntity().getFlags()));
		
		initializeRemote(sender);
	}
	
	protected abstract void initializeRemote(Communicator sender);
	
	private class ServerEntityObserver implements IEntityObserver
	{

		@Override
		public void enterWorld()
		{
			Core.getService(ServerGame.class).entityEnter(ServerEntity.this);
		}

		@Override
		public void leaveWorld()
		{
			Core.getService(ServerGame.class).entityLeave(ServerEntity.this);
		}

		@Override
		public void replaced() { }

		@Override
		public void flagSet(String name, int value) { }

		@Override
		public void flagCleared(String name) { }
		
	}
}
