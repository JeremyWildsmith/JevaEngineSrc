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
package jevarpg.net.client;

import java.util.List;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.communication.Communicator;
import jeva.communication.InvalidMessageException;
import jeva.communication.SharePolicy;
import jeva.communication.SharedClass;
import jeva.communication.SharedEntity;
import jeva.config.VariableStore;
import jeva.config.VariableValue;
import jeva.util.StaticSet;
import jeva.world.DialogicalEntity;
import jeva.world.DialogicalEntity.IDialogueObserver;
import jeva.world.Entity;
import jeva.world.World;
import jevarpg.library.RpgEntityLibrary;
import jevarpg.net.NetWorld;

@SharedClass(name = "World", policy = SharePolicy.ClientR)
public class ClientWorld extends NetWorld implements IClientShared
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

	@Override
	public SharedEntity getSharedEntity()
	{
		return this;
	}

	public boolean isReady()
	{
		return m_world != null;
	}

	public World getWorld()
	{
		return m_world;
	}

	@Override
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
			send(PrimitiveQuery.Initialize);
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

				final String map = ((InitializationArguments) message).getStore();

				new Thread()
				{
					@Override
					public void run()
					{
						m_world = World.create(new ClientRpgEntityLibrary(), VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(map)));
					}
				}.start();
			}
		}

		return true;
	}

	private class ClientRpgEntityLibrary extends RpgEntityLibrary
	{
		@Override
		public Entity createEntity(String entityName, @Nullable String instanceName, List<VariableValue> arguments)
		{
			Entity e = super.createEntity(entityName, instanceName, arguments);

			if (e instanceof DialogicalEntity)
				((DialogicalEntity) e).addObserver(new ClientEntityObserver(e));

			return e;
		}
	}

	private class ClientEntityObserver implements IDialogueObserver
	{
		private Entity m_entity;

		public ClientEntityObserver(Entity entity)
		{
			m_entity = entity;
		}

		@Override
		public void enterWorld()
		{
		}

		@Override
		public void leaveWorld()
		{
		}

		@Override
		public void taskBusyState(boolean isBusy)
		{
		}

		@Override
		public void onDialogEvent(Entity subject, int event)
		{
			send(new DialogEvent(m_entity.getName(), event, subject.getName()));
		}
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
