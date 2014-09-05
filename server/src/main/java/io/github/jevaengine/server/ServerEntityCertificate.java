package io.github.jevaengine.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.certificate.entity.HostWorldAssignment;
import io.github.jevaengine.netcommon.certificate.entity.InitializationArguments;
import io.github.jevaengine.netcommon.certificate.entity.Signal;
import io.github.jevaengine.netcommon.entity.NetEntityIdentifier;
import io.github.jevaengine.world.entity.DefaultEntity;
import io.github.jevaengine.world.entity.DefaultEntity.DefaultEntityBridge;

public abstract class ServerEntityCertificate<T extends DefaultEntity> extends ServerCertificate
{
	private IServerWorldLookup m_worldLookup;
	
	private ServerWorld m_hostWorld;
	private T m_entity;
	
	public <Y extends ServerCertificateBridge<Z>, Z extends ServerEntityCertificate<T>> ServerEntityCertificate(T entity, Y bridge)
	{
		super(bridge);
		m_worldLookup = Core.getService(ServerGame.class).getServerWorldLookup();
		m_entity = entity;
	}
	
	protected final T getTarget()
	{
		return m_entity;
	}
	
	@Override
	protected final void doLogic(int deltaTime)
	{	
		ServerWorld currentWorld = m_entity.isAssociated() ? m_worldLookup.lookupServerWorld(m_entity.getWorld()) : null;
		
		if(currentWorld != m_hostWorld)
			send(new HostWorldAssignment(currentWorld == null ? null : currentWorld.getName()));
	}

	@Override
	protected final void doSynchronization() { }

	@Override
	protected final boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if (recv instanceof Signal && (Signal)recv == Signal.InitializeRequest)
			initializeRemote(sender);
		else
			throw new InvalidMessageException(sender, recv, "Unidentified message from remote client");

		return true;
	}
	
	protected void initializeRemote(Communicator sender)
	{
		send(sender, new InitializationArguments(new NetEntityIdentifier(m_entity.getInstanceName())));
	}

	public static class ServerEntityCertificateBridge<Z extends ServerEntityCertificate<Y>, Y extends DefaultEntity> extends ServerCertificateBridge<Z>
	{
		public DefaultEntityBridge<? extends DefaultEntity> getEntity()
		{
			ServerEntityCertificate<? extends DefaultEntity> me = getCertificate();
			return me.m_entity.getBridge();
		}
	}
}
