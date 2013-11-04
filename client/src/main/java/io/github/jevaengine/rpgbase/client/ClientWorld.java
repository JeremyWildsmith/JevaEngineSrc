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
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.config.VariableStore;
import io.github.jevaengine.config.VariableValue;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.library.RpgEntityLibrary;
import io.github.jevaengine.rpgbase.netcommon.NetWorld;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.WorldDirection;
import io.github.jevaengine.world.Actor.IActorObserver;

import java.util.List;

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

			if (e instanceof Actor)
				((Actor) e).addObserver(new ClientEntityObserver(e));

			return e;
		}
	}

	private class ClientEntityObserver implements IActorObserver
	{
		private Entity m_entity;

		public ClientEntityObserver(Entity entity)
		{
			m_entity = entity;
		}

		@Override
		public void onDialogEvent(Entity subject, int event)
		{
			send(new DialogEvent(m_entity.getName(), event, subject.getName()));
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
		public void directionChanged(WorldDirection direction)
		{
		}

		@Override
		public void placement(Vector2F location)
		{
		}

		@Override
		public void moved(Vector2F delta)
		{
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
