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
package io.github.jevaengine.rpgbase.client;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.rpgbase.netcommon.NetWorld;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.World;

@SharedClass(name = "World", policy = SharePolicy.ClientR)
public final class ClientWorld extends NetWorld
{
	private static final int SYNC_INTERVAL = 200;

	private volatile World m_world;
	private volatile boolean m_isWorldLoading;

	private boolean m_dispatchedInit = false;

	private int m_tickCount = 0;

	private Observers m_observers = new Observers();

	public ClientWorld()
	{
		m_isWorldLoading = false;
	}

	public void addObserver(IClientWorldObserver observer)
	{
		m_observers.add(observer);
	}

	public void removeObserver(IClientWorldObserver observer)
	{
		m_observers.remove(observer);
	}

	public boolean isReady()
	{
		return m_world != null;
	}

	public World getWorld()
	{
		return m_world;
	}

	public void update(int deltaTime)
	{
		if (m_world != null && m_isWorldLoading)
		{
			m_isWorldLoading = false;
			m_observers.worldInitialized();
		}

		if (!m_dispatchedInit)
		{
			m_dispatchedInit = true;
			send(new InitializeRequest());
		}

		m_tickCount += deltaTime;

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object message) throws InvalidMessageException
	{
		if (message instanceof InitializationArguments)
		{
			if (m_world != null || m_isWorldLoading)
			{
				throw new InvalidMessageException(sender, message, "Server attempted to initialize multiple times.");
			} else
			{
				m_isWorldLoading = true;

				final String map = ((InitializationArguments) message).getWorldName();

				new Thread()
				{
					@Override
					public void run()
					{
						m_world = World.create(Core.getService(ResourceLibrary.class).openConfiguration(map));
					}
				}.start();
			}
		}

		return true;
	}

	private static class Observers extends StaticSet<IClientWorldObserver>
	{
		public void worldInitialized()
		{
			for (IClientWorldObserver o : this)
			{
				o.worldInitialized();
			}
		}
	}

	public interface IClientWorldObserver
	{
		void worldInitialized();
	}
}
