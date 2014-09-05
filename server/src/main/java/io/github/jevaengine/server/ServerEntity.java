package io.github.jevaengine.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.netcommon.entity.InitializeEntity;
import io.github.jevaengine.netcommon.entity.InitializeFlags;
import io.github.jevaengine.netcommon.entity.Signal;
import io.github.jevaengine.server.ServerCertificate.ServerCertificateBridge;
import io.github.jevaengine.world.entity.DefaultEntity;
import io.github.jevaengine.world.entity.DefaultEntity.DefaultEntityBridge;

public abstract class ServerEntity<T extends DefaultEntity> extends SharedEntity implements IDisposable
{
	private String m_className;
	
	private T m_entity;
	
	public ServerEntity(T entity, int syncInterval)
	{
		super(syncInterval);
		m_entity = entity;
		m_className = Core.getService(ResourceLibrary.class).lookup(entity.getClass());
	}
	
	public final T getEntity()
	{
		return m_entity;
	}

	public ServerEntityBridge getBridge()
	{
		return new ServerEntityBridge();
	}
	
	private boolean allowVisit(Communicator sender, INetVisitor<T> visitor)
	{
		return getDefaultVisitAuthorizer().isVisitorAuthorized(visitor, m_entity) || 
				Core.getService(ServerGame.class).getVisitAuthorizationPool().isVisitAuthorized(sender, visitor, m_entity);
	}
	
	@Override
	protected final boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if(recv instanceof INetVisitor<?>)
		{
			INetVisitor<?> genericVisitor = (INetVisitor<?>)recv;
			
			if(!genericVisitor.getHostComponentClass().isAssignableFrom(getEntity().getClass()))
				throw new InvalidMessageException(sender, recv, "Visitor cannot visit this component due to incompatable host component types");
			
			@SuppressWarnings("unchecked")
			INetVisitor<T> entityVisitor = (INetVisitor<T>)genericVisitor;
			
			if (allowVisit(sender, entityVisitor))
				entityVisitor.visit(sender, getEntity(), true);
			else
				throw new InvalidMessageException(sender, recv, "Dispatcher of visitor did not have sufficient ownership to perform task");
				
		}else if(recv instanceof Signal && (Signal)recv == Signal.InitializeRequest)
		{
			dispatchInitialization(sender);
		}else
			throw new InvalidMessageException(sender, recv, "Unrecognized message");

		return true;
	}
	
	private void dispatchInitialization(Communicator sender)
	{
		send(sender, new InitializeEntity(m_className, getEntity().getConfiguration(), getEntity().getInstanceName()));
		send(sender, new InitializeFlags(getEntity().getFlags()));
		
		initializeRemote(sender);
	}
	
	protected abstract ServerEntityCertificate<T> constructDefaultCertificate();
	protected abstract void initializeRemote(Communicator sender);
	protected abstract IVisitAuthorizer<INetVisitor<T>, T> getDefaultVisitAuthorizer();

	public final class ServerEntityBridge
	{
		public DefaultEntityBridge<?> getEntity()
		{
			return ServerEntity.this.getEntity().getBridge();
		}
		
		public ServerCertificateBridge<?> constructDefaultCertificate()
		{
			return ServerEntity.this.constructDefaultCertificate().getBridge();
		}
	}
}
