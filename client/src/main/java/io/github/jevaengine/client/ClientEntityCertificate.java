package io.github.jevaengine.client;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.netcommon.certificate.entity.HostWorldAssignment;
import io.github.jevaengine.netcommon.certificate.entity.InitializationArguments;
import io.github.jevaengine.netcommon.certificate.entity.Signal;
import io.github.jevaengine.netcommon.entity.NetEntityIdentifier;
import io.github.jevaengine.netcommon.world.NetWorldIdentifier;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.util.SynchronousExecutor.ISynchronousTask;

public abstract class ClientEntityCertificate extends SharedEntity implements IClientCertificate
{
	private static final int SYNC_INTERVAL = 50;
	
	private NetEntityIdentifier m_entity;
	private NetWorldIdentifier m_hostWorld;
	private Observers m_observers = new Observers();
	
	public ClientEntityCertificate()
	{
		super(SYNC_INTERVAL);
		
		enqueueLogicTask(new ISynchronousTask() {
			@Override
			public boolean run() {
				send(Signal.InitializeRequest);
				return true;
			}
		});
	}
	
	@Nullable
	public NetEntityIdentifier getEntity()
	{
		return m_entity;
	}
	
	@Nullable
	public NetWorldIdentifier getHostWorld()
	{
		return m_hostWorld;
	}
	
	@Override
	protected void doLogic(int deltaTime) { }

	@Override
	protected void doSynchronization() throws InvalidMessageException { }

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if(recv instanceof InitializationArguments)
		{
			if(m_entity != null)
				throw new InvalidMessageException(sender, recv, "Certificate cannot be initialized twice.");
			
			m_entity = ((InitializationArguments)recv).getEntityName();
		}else if(recv instanceof HostWorldAssignment)
		{
			m_hostWorld = ((HostWorldAssignment)recv).getHostWorld();
			m_observers.contextChanged();
		}
		
		return true;
	}
	
	@Override
	public boolean isInitialized()
	{
		return m_entity != null;
	}
	
	public interface IClientEntityCertificateObserver
	{
		void contextChanged();
	}
	
	private class Observers extends StaticSet<IClientEntityCertificateObserver>
	{
		void contextChanged()
		{
			for(IClientEntityCertificateObserver o : this)
				o.contextChanged();
		}
	}
}
