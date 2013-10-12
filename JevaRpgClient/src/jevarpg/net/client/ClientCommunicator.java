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

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.istack.internal.Nullable;

import jeva.communication.Communicator;
import jeva.communication.InvalidMessageException;
import jeva.communication.ShareEntityException;
import jeva.communication.SharedEntity;
import jeva.communication.SnapshotSynchronizationException;
import jeva.util.StaticSet;
import jeva.world.IWorldAssociation;
import jeva.world.World;
import jevarpg.net.NetUser.UserCredentials;
import jevarpg.net.client.ClientWorld.IClientWorldObserver;

public class ClientCommunicator extends Communicator
{
	private static final int SYNC_INTERVAL = 20;

	private final Logger m_logger = LoggerFactory.getLogger(ClientCommunicator.class);

	private ClientWorld m_world;

	private int m_tickCount = 0;

	private StaticSet<IClientShared> m_sharedEntities = new StaticSet<IClientShared>();

	private ArrayList<IWorldAssociation> m_associatedEntities = new ArrayList<IWorldAssociation>();

	private UserCredentials m_credentials;

	@Nullable
	private ClientUser m_servedUser;

	private WorldObserver m_worldObserver = new WorldObserver();

	private Observers m_observers = new Observers();
	
	private EntityShareHandler m_shareHandler = new EntityShareHandler();

	public ClientCommunicator()
	{
		registerClass(ClientRpgCharacter.class);
		registerClass(ClientWorld.class);
		registerClass(ClientUser.class);
		
		this.addObserver(m_shareHandler);
	}
	
	public void addObserver(IClientCommunicatorObserver o)
	{
		m_observers.add(o);
	}

	public void removeObserver(IClientCommunicatorObserver o)
	{
		m_observers.remove(o);
	}

	@Override
	protected boolean isServer()
	{
		return false;
	}

	private void unserveWorld()
	{
		if (m_world != null)
		{
			m_observers.unservedWorld();
			m_world.removeObserver(m_worldObserver);
			m_world = null;
		}
	}

	protected void disconnect(String cause)
	{
		unserveWorld();

		m_observers.disconnected(cause);

		m_sharedEntities.clear();
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

			for (IClientShared entity : m_sharedEntities)
			{
				entity.getSharedEntity().synchronize();
				entity.update(deltaTime);
			}
		} catch (InvalidMessageException e)
		{
			String error = String.format("Recieved invalid message from server: %s", e.getMessage());

			m_logger.error(error);
			disconnect(error);
		}
	}

	public void setUserCredentials(UserCredentials credentials)
	{
		m_credentials = credentials;
	}

	@Override
	protected SharedEntity instantiatePair(Class<?> entityClass) throws ShareEntityException
	{
		if (entityClass == ClientWorld.class)
			return new ClientWorld();
		else if (entityClass == ClientRpgCharacter.class)
			return new ClientRpgCharacter();
		else if (entityClass == ClientUser.class)
			return new ClientUser(m_credentials);
		else
			return super.instantiatePair(entityClass);
	}

	private class EntityShareHandler implements ICommunicatorObserver
	{
		@Override
		public void entityUnshared(SharedEntity entity)
		{
			if (!(entity instanceof IClientShared))
				disconnect("Server shared unrecognized entity.");
			else
			{
				m_sharedEntities.remove((IClientShared) entity);

				if (entity instanceof ClientWorld)
				{
					unserveWorld();
				} else if (entity instanceof ClientRpgCharacter)
				{
					ClientRpgCharacter character = (ClientRpgCharacter) entity;

					m_associatedEntities.remove(character);

					if (character.isAssociated())
						character.disassociate();
				} else if (entity instanceof ClientRpgCharacter)
				{
					m_observers.unservedUser();
					m_servedUser = null;
				}
			}
		}
		
		@Override
		public void entityShared(SharedEntity entity)
		{
			if (!(entity instanceof IClientShared))
				disconnect("Server shared unrecognized entity.");
			else
			{
				m_sharedEntities.add((IClientShared) entity);
	
				if (entity instanceof ClientWorld)
				{
					if (m_world != null)
						disconnect("Server shared world when world has already been initialized");
					else
					{
						ClientWorld world = (ClientWorld) entity;
	
						m_world = world;
						m_world.addObserver(m_worldObserver);
					}
				} else if (entity instanceof ClientRpgCharacter)
				{
					ClientRpgCharacter character = (ClientRpgCharacter) entity;
					m_associatedEntities.add(character);
	
					if (m_world.isReady())
						character.associate(m_world.getWorld());
					
				} else if (entity instanceof ClientUser)
				{
					m_servedUser = (ClientUser) entity;
					m_observers.servedUser((ClientUser) entity);
				}
			}
		}
	}

	private class WorldObserver implements IClientWorldObserver
	{
		@Override
		public void worldInitialized()
		{
			for (IWorldAssociation e : m_associatedEntities)
				e.associate(m_world.getWorld());

			m_observers.servedWorld(m_world.getWorld());
		}
	}

	private static class Observers extends StaticSet<IClientCommunicatorObserver>
	{
		public void servedWorld(final World world)
		{
			for (IClientCommunicatorObserver o : Observers.this)
				o.servedWorld(world);
		}

		public void unservedWorld()
		{
			for (IClientCommunicatorObserver o : Observers.this)
				o.unservedWorld();

		}

		public void servedUser(final ClientUser user) {

			for (IClientCommunicatorObserver o : Observers.this)
				o.servedUser(user);
		}

		public void unservedUser() {
			for (IClientCommunicatorObserver o : Observers.this)
				o.unservedUser();
		}

		public void disconnected(final String cause) {
			for (IClientCommunicatorObserver o : Observers.this)
				o.disconnected(cause);
		}
	}

	public interface IClientCommunicatorObserver
	{
		void servedUser(ClientUser user);

		void unservedUser();

		void servedWorld(World world);

		void unservedWorld();

		void disconnected(String cause);
	}
}
