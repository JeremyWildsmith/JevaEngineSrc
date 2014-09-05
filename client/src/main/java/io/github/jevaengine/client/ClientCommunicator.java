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
import io.github.jevaengine.client.library.IClientLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.communication.SnapshotSynchronizationException;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientCommunicator extends Communicator
{
	private static final int SYNC_INTERVAL = 20;

	private final Logger m_logger = LoggerFactory.getLogger(ClientCommunicator.class);

	private int m_tickCount = 0;

	@Nullable private ClientUser m_user;

	private Observers m_observers = new Observers();

	public ClientCommunicator()
	{
		registerClass(ClientWorld.class);
		registerClass(ClientUser.class);
		
		for(Class<? extends SharedEntity> c : Core.getService(IClientLibrary.class).getSharedClasses())
			registerClass(c);
		
		this.addObserver(new EntityShareHandler());
	}

	@Nullable ClientUser getUser()
	{
		return m_user;
	}
	
	@Override
	protected boolean isServer()
	{
		return false;
	}

	protected void disconnect(String cause)
	{
		unbind();
		m_observers.disconnected(cause);
	}

	public void update(int deltaTime)
	{
		poll();

		try
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
					disconnect("Error synchronizing client");
				}
			}

			if(m_user != null)
			{
				m_user.synchronize();
				m_user.update(deltaTime);
			}
			
		} catch (InvalidMessageException e)
		{
			String error = String.format("Recieved invalid message from server: %s", e.getMessage());

			m_logger.error(error);
			disconnect(error);
		}
	}

	private class EntityShareHandler implements ICommunicatorObserver
	{
		@Override
		public void entityUnshared(SharedEntity entity)
		{
			if (entity instanceof ClientUser)
				m_user = null;
		}

		@Override
		public void entityShared(SharedEntity entity)
		{
			if (entity instanceof ClientUser)
				m_user = (ClientUser)entity;
		}
	}
	
	private static class Observers extends StaticSet<IClientCommunicatorObserver>
	{
		public void disconnected(final String cause)
		{
			for (IClientCommunicatorObserver o : Observers.this)
				o.disconnected(cause);
		}
	}

	public interface IClientCommunicatorObserver
	{	
		void disconnected(String cause);
	}
}
