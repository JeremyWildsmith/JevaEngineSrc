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
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.client.ClientCommunicator.IClientCommunicatorObserver;
import io.github.jevaengine.rpgbase.client.ClientUser.IClientUserObserver;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.World;

import java.awt.Color;

public class LoadingState implements IGameState
{
	private Window m_connectingWindow;
	private ClientGame m_context;

	private EventHandler m_handler = new EventHandler();

	public LoadingState(@Nullable ClientUser user, @Nullable String playerEntity, @Nullable World world)
	{
		m_connectingWindow = new Window(Core.getService(Game.class).getGameStyle(), 300, 50);
		m_connectingWindow.setMovable(false);
		m_connectingWindow.setRenderBackground(false);
		m_connectingWindow.addControl(new Label("Loading - May take some time", Color.white));
		m_connectingWindow.setLocation(new Vector2D(240, 370));

		m_handler = new EventHandler(user, playerEntity, world);
	}

	public LoadingState(@Nullable ClientUser user, @Nullable String playerEntity)
	{
		this(user, playerEntity, null);
	}

	public LoadingState()
	{
		this(null, null, null);
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

		Core.getService(IWindowManager.class).addWindow(m_connectingWindow);

		m_context.getCommunicator().addObserver(m_handler);

		m_handler.checkReady();
	}

	@Override
	public void leave()
	{
		Core.getService(IWindowManager.class).removeWindow(m_connectingWindow);
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
		@Nullable private ClientUser m_user;

		@Nullable private World m_world;

		@Nullable private String m_playerEntity;

		public EventHandler()
		{

		}

		public EventHandler(@Nullable ClientUser user, @Nullable String playerEntity, @Nullable World world)
		{
			m_user = user;
			m_world = world;
			m_playerEntity = playerEntity;
		}

		public void checkReady()
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
