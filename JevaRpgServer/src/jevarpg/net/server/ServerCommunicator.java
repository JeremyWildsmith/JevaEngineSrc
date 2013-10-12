package jevarpg.net.server;

import java.io.IOException;

import com.sun.istack.internal.Nullable;

import jeva.communication.Communicator;
import jeva.communication.SnapshotSynchronizationException;

public class ServerCommunicator extends Communicator
{
	private static final int SYNC_INTERVAL = 50;

	private int m_tickCount;

	@Nullable
	private ServerGame m_server;

	public ServerCommunicator(ServerGame server)
	{
		m_server = server;

		registerClass(ServerWorld.class);
		registerClass(ServerRpgCharacter.class);
		registerClass(ServerUser.class);
	}

	public void update(int deltaTime)
	{
		m_tickCount += deltaTime;

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			try
			{
				snapshot();
			} catch (IOException | SnapshotSynchronizationException e)
			{
				disconnect("Error synchronizing with client");
			}
		}
	}

	protected void disconnect(String reason)
	{
		if (m_server != null)
			m_server.closeConnection(this, reason);

		m_server = null;
	}

	protected boolean isConnected()
	{
		return m_server != null;
	}

	@Override
	protected boolean isServer()
	{
		return true;
	}
}
