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

import io.github.jevaengine.Core;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.PolicyViolationException;
import io.github.jevaengine.communication.ShareEntityException;
import io.github.jevaengine.communication.tcp.RemoteSocketCommunicator;
import io.github.jevaengine.config.VariableStore;
import io.github.jevaengine.game.IGameScriptProvider;
import io.github.jevaengine.ui.CommandMenu;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.RpgGame;
import io.github.jevaengine.rpgbase.netcommon.NetUser.UserCredentials;
import io.github.jevaengine.rpgbase.server.ServerUser.IUserHandler;
import io.github.jevaengine.rpgbase.server.ui.WorldViewWindow;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.Entity.IEntityObserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerGame extends RpgGame implements IDisposable
{
	protected static final int PORT = 1554;

	private final Logger m_logger = LoggerFactory.getLogger(ServerGame.class);

	private final StaticSet<RemoteClient> m_clients = new StaticSet<RemoteClient>();
	private final StaticSet<ServerWorld> m_worlds = new StaticSet<ServerWorld>();
	
	private CommandMenu m_commandMenu;
	
	private ServerListener m_listener;

	public void startup()
	{
		super.startup();

		UIStyle styleSmall = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/small.juis")));

		m_commandMenu = new CommandMenu(styleSmall, 660, 125);
		m_commandMenu.setLocation(new Vector2D(10, 10));

		Core.getService(IWindowManager.class).addWindow(m_commandMenu);

		ServerSocket serverSocket;
		try
		{
			serverSocket = new ServerSocket(PORT);

			m_listener = new ServerListener(serverSocket);

		} catch (IOException e)
		{
			m_logger.error("Unable to initialize server: " + e.toString());
		}
	}

	@Override
	public void dispose()
	{
		m_listener.stop();
	}

	private void openWorldViewWindow(World world)
	{
		Core.getService(IWindowManager.class).addWindow(new WorldViewWindow(world));
	}

	public ServerWorld getServerWorld(String worldName)
	{
		String formalName = worldName.trim().replace('\\', '/');

		synchronized (m_worlds)
		{
			for (ServerWorld world : m_worlds)
			{
				if (world.getName().equals(formalName))
				{
					return world;
				}
			}

			ServerWorld newWorld = new ServerWorld(this, formalName);

			m_worlds.add(newWorld);

			return newWorld;
		}
	}

	public ServerWorld getServerWorld(World world)
	{
		synchronized (m_worlds)
		{
			for (ServerWorld serverWorld : m_worlds)
			{
				if (serverWorld.getWorld() == world)
				{
					return serverWorld;
				}
			}

			throw new NoSuchElementException();
		}
	}

	private void syncWorld(ServerWorld world)
	{
		try
		{
			world.synchronize();
		} catch (InvalidMessageException e)
		{
			ServerCommunicator sender = (ServerCommunicator) e.getSender();

			if (sender.isConnected())
				sender.disconnect("Invalid Message: " + e.toString());

			syncWorld(world);
		}
	}

	@Override
	public void update(int deltaTime)
	{
		for (RemoteClient client : m_clients)
		{
			client.update(deltaTime);
		}

		synchronized (m_worlds)
		{
			for (ServerWorld world : m_worlds)
			{
				syncWorld(world);
				world.update(deltaTime);
			}

			super.update(deltaTime);
		}
	}

	@Override
	public void keyUp(InputKeyEvent e)
	{
		super.keyUp(e);

		if (e.isConsumed)
			return;

		if (e.keyChar == '`')
		{
			m_commandMenu.setVisible(!m_commandMenu.isVisible());
			e.isConsumed = true;
		}
	}

	@Override
	public RpgCharacter getPlayer()
	{
		return null;
	}

	public void entityEnter(ServerRpgCharacter character)
	{
		getServerWorld(character.getCharacter().getWorld()).entityEnter(character);
	}

	public void entityLeave(ServerRpgCharacter character)
	{
		getServerWorld(character.getCharacter().getWorld()).entityLeave(character);
	}

	@Nullable
	private RemoteClient getClient(RemoteSocketCommunicator com)
	{
		for (RemoteClient client : m_clients)
		{
			if (client.m_remote == com)
				return client;
		}

		return null;
	}

	@Nullable
	private RemoteClient getClient(ServerCommunicator com)
	{
		synchronized (m_clients)
		{
			for (RemoteClient client : m_clients)
			{
				if (client.m_communicator == com)
					return client;
			}
		}

		return null;
	}

	private void openConnection(RemoteSocketCommunicator remote)
	{
		synchronized (m_clients)
		{
			try
			{
				RemoteClient client = new RemoteClient(remote);

				m_clients.add(client);
			} catch (IOException | ShareEntityException | PolicyViolationException e)
			{
				closeConnection(remote, "Internal Server Error: " + e.toString());
			}
		}
	}

	public void serverSay(String message)
	{
		synchronized (m_clients)
		{
			for (RemoteClient client : m_clients)
			{
				if (client.m_user.isAuthenticated())
				{
					client.m_user.sendChatMessage("{SERVER}", message);
				}
			}
		}
	}

	void closeConnection(RemoteClient client, @Nullable String reason)
	{
		client.dispose();
		m_clients.remove(client);

		m_logger.info("Closed remote client connection: " + reason == null ? "No exceptional reason" : reason);

	}

	void closeConnection(RemoteSocketCommunicator connection, @Nullable String reason)
	{
		RemoteClient client = getClient(connection);

		if (client != null)
			closeConnection(client, reason);
		else
		{
			m_logger.warn("Closed unrecognized raw remote communicator: " + reason);
			connection.dispose();
		}
	}

	void closeConnection(RemoteSocketCommunicator connection)
	{
		closeConnection(connection, null);
	}

	void closeConnection(ServerCommunicator connection, @Nullable String reason)
	{
		RemoteClient client = getClient(connection);

		synchronized (m_clients)
		{

			if (client != null)
			{
				client.dispose();
				m_clients.remove(client);

				m_logger.info("Closed remote client connection: " + reason == null ? "No exceptional reason" : reason);
			} else
				throw new NoSuchElementException();
		}
	}

	void closeConnection(ServerCommunicator connection)
	{
		closeConnection(connection, null);
	}

	@Override
	public IGameScriptProvider getScriptBridge()
	{
		return new ServerGameScriptProvider();
	}

	@Override
	public void mouseButtonStateChanged(InputMouseEvent e)
	{
	}

	public class ServerGameScriptProvider extends RpgGameScriptProvider
	{
		@Override
		public Object getGameBridge()
		{
			return new ServerGameBridge();
		}

		public class ServerGameBridge extends GameBridge
		{
			public void openWorldView(String world)
			{
				openWorldViewWindow(ServerGame.this.getServerWorld(world).getWorld());
			}

			public void loadWorld(String world)
			{
				ServerGame.this.getServerWorld(world);
			}
			
			public World.WorldScriptContext getWorld(String name)
			{
				return ServerGame.this.getServerWorld(name).getWorld().getScriptBridge();
			}

			public RpgCharacter.EntityBridge<?> getClientPlayer(String name)
			{

				synchronized (m_clients)
				{
					for (RemoteClient client : m_clients)
					{
						if (client.getUser().isAuthenticated() && client.isInitialized() && client.getUser().getUsername().toLowerCase().compareTo(name.toLowerCase()) == 0)
							return client.getCharacter().getScriptBridge();
					}
				}

				return null;
			}

			public void say(String message)
			{
				serverSay(message);
			}
		}
	}

	private class RemoteClient implements IDisposable
	{
		private ServerCommunicator m_communicator;
		private RemoteSocketCommunicator m_remote;
		private ServerUser m_user;

		@Nullable private ServerRpgCharacter m_character;

		private boolean m_isRemoteInitialized = false;

		private UserHandler m_userHandler;

		private RemoteClient(RemoteSocketCommunicator remote) throws IOException, ShareEntityException, PolicyViolationException
		{
			m_remote = remote;
			m_communicator = new ServerCommunicator(ServerGame.this);
			m_communicator.bind(remote);

			try
			{
				m_userHandler = new UserHandler();
				m_user = new ServerUser(m_userHandler, m_communicator);
				m_communicator.shareEntity(m_user);

			} catch (IOException | PolicyViolationException | ShareEntityException e)
			{
				m_communicator.unbind();
				throw e;
			}
		}

		@Override
		public void dispose()
		{
			if (m_character != null && m_character.getCharacter().isAssociated())
			{
				m_character.getCharacter().getWorld().removeEntity(m_character.getControlledEntity());
				m_character.getCharacter().removeObserver(m_userHandler);
			}
			m_communicator.unbind();
			m_remote.dispose();
		}

		public void update(int deltaTime)
		{
			try
			{
				m_user.synchronize();

			} catch (InvalidMessageException e)
			{
				closeConnection(RemoteClient.this, "Invalid Message: " + e.toString());
			}

			m_communicator.update(deltaTime);
			m_user.update(deltaTime);

			if (m_user.isAuthenticated() && !m_isRemoteInitialized)
			{
				m_isRemoteInitialized = true;

				m_character = new ServerRpgCharacter(m_user.getUsername(),
						"PLAYER_" + m_user.getUsername().toLowerCase(),
						ServerGame.this,
						"npcs/player.jnpc");

				m_character.getCharacter().addObserver(m_userHandler);
				m_character.getCharacter().setLocation(new Vector2F(6, 6));
				m_character.setOwner(m_communicator);

				m_user.assignEntity(m_character.getCharacter().getName());

				ServerWorld spawnWorld = getServerWorld("map/outsideClosed.jmp");

				spawnWorld.getWorld().addEntity(m_character.getControlledEntity());

			} else if (m_character != null && m_character.getCharacter().isDead())
			{
				RpgCharacter character = m_character.getCharacter();

				ServerWorld currentWorld = character.isAssociated() ? getServerWorld(character.getWorld()) : null;
				ServerWorld spawnWorld = getServerWorld("map/outsideClosed.jmp");

				if (currentWorld != spawnWorld)
				{
					if (currentWorld != null)
						currentWorld.getWorld().removeEntity(character);

					spawnWorld.getWorld().addEntity(character);
				}

				character.setLocation(new Vector2F(6, 6));
				character.setHealth((int) (character.getMaxHealth() * 0.3F));

				serverSay("Ermahgerd. " + m_user.getUsername() + " died!");
			}
		}

		public boolean isInitialized()
		{
			return m_isRemoteInitialized;
		}

		public RpgCharacter getCharacter()
		{
			return m_character.getCharacter();
		}

		public ServerUser getUser()
		{
			return m_user;
		}

		private class UserHandler implements IUserHandler, IEntityObserver
		{
			@Override
			public boolean loginUser(UserCredentials credentials)
			{
				final String username = credentials.getNickname();

				synchronized (m_clients)
				{
					for (RemoteClient client : m_clients)
					{
						if (!client.m_user.isAuthenticated())
							continue;

						String clientUsername = client.m_user.getUsername();

						if (clientUsername.toLowerCase().compareTo(username.toLowerCase()) == 0)
							return false;
					}

					return username.length() <= 16 && username.length() >= 3 && username.matches("[a-zA-Z0-9]*");
				}
			}

			@Override
			public void recieveChatMessage(String message)
			{
				synchronized (m_clients)
				{
					for (RemoteClient client : m_clients)
					{
						if (client.m_user.isAuthenticated())
						{
							client.m_user.sendChatMessage(RemoteClient.this.m_user.getUsername(), message);
						}
					}
				}
			}

			@Override
			public void enterWorld()
			{
				try
				{
					m_communicator.shareEntity(getServerWorld(m_character.getCharacter().getWorld()));
				} catch (ShareEntityException | IOException e)
				{
					closeConnection(RemoteClient.this, "Error occured attempting to share with client: " + e.toString());
				}
			}

			@Override
			public void leaveWorld()
			{
				try
				{
					m_communicator.unshareEntity(getServerWorld(m_character.getCharacter().getWorld()));
				} catch (IOException e)
				{
					closeConnection(RemoteClient.this, "Error occured attempting to share with client: " + e.toString());
				}
			}
		}
	}

	private class ServerListener
	{
		private ServerSocket m_socket;

		private volatile boolean m_queryStop = false;

		private Listener m_listener;

		public ServerListener(ServerSocket socket)
		{
			m_socket = socket;

			m_listener = new Listener();

			m_logger.info("Server accepting connections");

			new Thread(m_listener).start();
		}

		public void stop()
		{
			m_queryStop = true;

			try
			{
				m_socket.close();
			} catch (IOException e)
			{
			}
		}

		private class Listener implements Runnable
		{
			@Override
			public void run()
			{
				while (!m_queryStop)
				{
					try
					{
						Socket remoteSocket = m_socket.accept();
						remoteSocket.setTcpNoDelay(true);
						openConnection(new RemoteSocketCommunicator(remoteSocket));

					} catch (IOException e)
					{
						if (!m_queryStop)
							m_logger.warn("Server listener ended prematurely");

						m_queryStop = true;
					}
				}
			}
		}
	}
}
