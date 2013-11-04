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
package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.SnapshotSynchronizationException;
import io.github.jevaengine.util.Nullable;

import java.io.IOException;

public class ServerCommunicator extends Communicator
{
	private static final int SYNC_INTERVAL = 50;

	private int m_tickCount;

	@Nullable private ServerGame m_server;

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
