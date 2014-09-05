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
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.communication.SnapshotSynchronizationException;
import io.github.jevaengine.server.library.IServerLibrary;

import java.io.IOException;

public class ServerCommunicator extends Communicator
{
	private static final int SYNC_INTERVAL = 50;

	private int m_tickCount;

	public ServerCommunicator()
	{
		registerClass(ServerWorld.class);
		registerClass(ServerUser.class);
		
		for(Class<? extends SharedEntity> c : Core.getService(IServerLibrary.class).getSharedClasses())
			registerClass(c);
	}

	public void update(int deltaTime) throws SnapshotSynchronizationException, IOException
	{
		m_tickCount += deltaTime;

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;

			snapshot();
		}
	}

	@Override
	protected boolean isServer()
	{
		return true;
	}
}
