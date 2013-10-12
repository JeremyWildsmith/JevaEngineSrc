package jevarpg.net.client;

import java.awt.Color;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.IDisposable;
import jeva.IResourceLibrary;
import jeva.config.VariableStore;
import jeva.graphics.ui.Label;
import jeva.graphics.ui.UIStyle;
import jeva.graphics.ui.Window;
import jeva.math.Vector2D;
import jeva.world.World;
import jevarpg.net.client.ClientCommunicator.IClientCommunicatorObserver;
import jevarpg.net.client.ClientUser.IClientUserObserver;

public class LoadingState implements IGameState
{
	private Window m_connectingWindow;
	private ClientGame m_context;

	private EventHandler m_handler = new EventHandler();

	public LoadingState()
	{
		final UIStyle styleLarge = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/large.juis")));

		m_connectingWindow = new Window(styleLarge, 300, 50);
		m_connectingWindow.setMovable(false);
		m_connectingWindow.setRenderBackground(false);
		m_connectingWindow.addControl(new Label("Loading - May take some time", Color.white));
		m_connectingWindow.setLocation(new Vector2D(240, 370));
	}

	private void loadingCompleted(String playerEntityName, ClientUser user, World world)
	{
		m_context.getCommunicator().removeObserver(m_handler);
		m_handler.dispose();

		m_context.setState(new PlayingState(playerEntityName, user, world));
	}

	@Override
	public void enter(ClientGame context)
	{
		m_context = context;

		context.getWindowManager().addWindow(m_connectingWindow);

		m_context.getCommunicator().addObserver(m_handler);
	}

	@Override
	public void leave()
	{
		m_context.getWindowManager().removeWindow(m_connectingWindow);
		m_context.getCommunicator().removeObserver(m_handler);
		m_handler.dispose();
		m_context = null;
	}

	@Override
	public void update(int deltaTime)
	{
		m_context.getCommunicator().poll();
	}

	private class EventHandler implements IClientUserObserver, IClientCommunicatorObserver, IDisposable
	{
		@Nullable
		private ClientUser m_user;
		@Nullable
		private World m_world;
		@Nullable
		private String m_playerEntity;

		private void checkReady()
		{
			if (m_user != null && m_user.isAuthenticated() && m_world != null && m_playerEntity != null)
			{
				loadingCompleted(m_playerEntity, m_user, m_world);
			}
		}

		@Override
		public void dispose()
		{
			if (m_user != null)
				m_user.removeObserver(this);
		}

		@Override
		public void timeout()
		{
			m_context.getCommunicator().disconnect("Timeout");
		}

		@Override
		public void disconnected(String cause)
		{
			m_context.setState(new LoginState(cause));
		}

		@Override
		public void servedUser(ClientUser user)
		{
			m_user = user;
			user.addObserver(this);

			checkReady();
		}

		@Override
		public void unservedUser()
		{
			m_user.removeObserver(this);
			m_user = null;
		}

		@Override
		public void servedWorld(World world)
		{
			m_world = world;

			checkReady();
		}

		@Override
		public void unservedWorld()
		{
			m_world = null;
		}

		@Override
		public void authenticated()
		{
			checkReady();
		}

		@Override
		public void authenticationFailed()
		{
			m_context.getCommunicator().disconnect("Authentication Failed.");
		}

		@Override
		public void recieveChatMessage(String user, String message)
		{
		}

		@Override
		public void assignedPlayer(String entityName)
		{
			m_playerEntity = entityName;

			checkReady();
		}

		@Override
		public void unassignedPlayer()
		{
			m_playerEntity = null;
		}
	}
}
