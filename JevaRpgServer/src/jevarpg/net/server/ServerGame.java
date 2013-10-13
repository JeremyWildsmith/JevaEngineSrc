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
package jevarpg.net.server;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import proguard.annotation.KeepClassMemberNames;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.IDisposable;
import jeva.IResourceLibrary;
import jeva.communication.InvalidMessageException;
import jeva.communication.PolicyViolationException;
import jeva.communication.ShareEntityException;
import jeva.communication.network.RemoteSocketCommunicator;
import jeva.config.VariableStore;
import jeva.game.ControlledCamera;
import jeva.game.IGameScriptProvider;
import jeva.game.IWorldCamera;
import jeva.graphics.ui.CommandMenu;
import jeva.graphics.ui.UIStyle;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.util.StaticSet;
import jeva.world.World;
import jevarpg.RpgCharacter;
import jevarpg.RpgGame;
import jevarpg.net.NetUser.UserCredentials;
import jevarpg.net.server.ServerUser.IUserHandler;

public class ServerGame extends RpgGame implements IDisposable
{
	protected static final int PORT = 1554;

	private final Logger m_logger = LoggerFactory.getLogger(ServerGame.class);

	private CommandMenu m_commandMenu;

	private Vector2F m_cameraMovement = new Vector2F();

	private ControlledCamera m_camera = new ControlledCamera();

	private StaticSet<RemoteClient> m_clients = new StaticSet<RemoteClient>();
	private ServerListener m_listener;

	private StaticSet<ServerWorld> m_worlds = new StaticSet<ServerWorld>();

	public void startup()
	{
		super.startup();

		UIStyle styleSmall = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/small.juis")));

		m_commandMenu = new CommandMenu(styleSmall, 660, 125);
		m_commandMenu.setLocation(new Vector2D(10, 10));

		getWindowManager().addWindow(m_commandMenu);

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
		if (!m_cameraMovement.isZero())
			m_camera.move(m_cameraMovement.normalize().multiply(0.3F));

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
	protected void worldSelection(InputMouseEvent e, Vector2D location)
	{
	}

	@Override
	protected IWorldCamera getCamera()
	{
		return m_camera;
	}

	@Override
	public void keyDown(InputKeyEvent e)
	{
		super.keyDown(e);

		if (!e.isConsumed)
		{
			switch (e.keyCode)
			{
			case KeyEvent.VK_UP:
				e.isConsumed = true;
				m_cameraMovement.y = -1;
				break;
			case KeyEvent.VK_RIGHT:
				e.isConsumed = true;
				m_cameraMovement.x = 1;
				break;
			case KeyEvent.VK_DOWN:
				e.isConsumed = true;
				m_cameraMovement.y = 1;
				break;
			case KeyEvent.VK_LEFT:
				e.isConsumed = true;
				m_cameraMovement.x = -1;
				break;
			}
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
		} else
		{

			switch (e.keyCode)
			{
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				e.isConsumed = true;
				m_cameraMovement.y = 0;
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_LEFT:
				e.isConsumed = true;
				m_cameraMovement.x = 0;
				break;
			}
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

	public void closeConnection(RemoteClient client, @Nullable String reason)
	{
		client.dispose();
		m_clients.remove(client);

		m_logger.info("Closed remote client connection: " + reason == null ? "No exceptional reason" : reason);

	}

	public void closeConnection(RemoteSocketCommunicator connection, @Nullable String reason)
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

	public void closeConnection(RemoteSocketCommunicator connection)
	{
		closeConnection(connection, null);
	}

	public void closeConnection(ServerCommunicator connection, @Nullable String reason)
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

	public void closeConnection(ServerCommunicator connection)
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

		@KeepClassMemberNames
		public class ServerGameBridge extends GameBridge
		{
			public void setWorldView(String world)
			{
				ServerGame.this.setWorld(ServerGame.this.getServerWorld(world).getWorld());
			}

			public void clearWorldView()
			{
				ServerGame.this.clearWorld();
			}

			public void loadWorld(String world)
			{
				ServerGame.this.getServerWorld(world);
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

		@Nullable
		private ServerRpgCharacter m_character;

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
				m_character.getCharacter().getWorld().removeEntity(m_character.getControlledEntity());

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
				
				m_character.getCharacter().setLocation(new Vector2F(6, 6));
				m_character.setOwner(m_communicator);

				m_user.assignEntity(m_character.getCharacter().getName());
				
				ServerWorld spawnWorld = getServerWorld("map/outsideClosed.jmp");
				spawnWorld.getWorld().addEntity(m_character.getControlledEntity());

				try
				{
					m_communicator.shareEntity(spawnWorld);
				} catch (ShareEntityException | IOException e)
				{
					closeConnection(RemoteClient.this, "Error occured attempting to share with client: " + e.toString());
				}

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

		private class UserHandler implements IUserHandler
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
