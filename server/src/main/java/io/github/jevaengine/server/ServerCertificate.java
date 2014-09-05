package io.github.jevaengine.server;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.script.ScriptHiddenMember;

@SharedClass(name = "EntityRightsCertificate", policy = SharePolicy.ClientR)
public abstract class ServerCertificate extends SharedEntity
{
	private static final int SYNC_INTERVAL = 50;
	
	private ServerCertificateBridge<?> m_bridge;
	
	public <T extends ServerCertificateBridge<?>> ServerCertificate(T bridge)
	{
		super(SYNC_INTERVAL);
		
		m_bridge = bridge;
		m_bridge.setCertificate(this);
	}

	public ServerCertificateBridge<?> getBridge()
	{
		return m_bridge;
	}
	
	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		throw new InvalidMessageException(sender, recv, "Unrecognized message.");
	}
	
	@Override
	protected final void onCommunicatorBound(Communicator communicator)
	{
		authorizeCommunicator(communicator);
	}

	@Override
	protected final void onCommunicatorUnbound(Communicator communicator)
	{
		deauthorizeCommunicator(communicator);
	}
	
	protected abstract void authorizeCommunicator(Communicator communicator);
	protected abstract void deauthorizeCommunicator(Communicator communicator);
	
	public static class ServerCertificateBridge<T extends ServerCertificate>
	{
		private ServerCertificate m_me;
		
		private void setCertificate(ServerCertificate certificate)
		{
			m_me = certificate;
		}
		
		@SuppressWarnings("unchecked")
		@ScriptHiddenMember
		public T getCertificate()
		{
			return (T)m_me;
		}
	}
}
