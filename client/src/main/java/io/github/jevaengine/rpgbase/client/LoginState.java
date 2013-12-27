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
import io.github.jevaengine.audio.Audio;
import io.github.jevaengine.communication.tcp.RemoteSocketCommunicator;
import io.github.jevaengine.game.ControlledCamera;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Viewport;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.RpgLibrary;
import io.github.jevaengine.rpgbase.client.ClientGame.ClientConfiguration;
import io.github.jevaengine.rpgbase.netcommon.NetUser.UserCredentials;
import io.github.jevaengine.world.World;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;

public final class LoginState implements IGameState
{

	private ClientGame m_context;

	//private Audio m_backgroundMusic;

	private Window m_loginWindow;

	private TextArea m_connectingAbout;

	private TextArea m_nickname;

	private Window m_overlayWindow;
	private Window m_worldViewWindow;

	private World m_menuWorld;

	public LoginState()
	{
		this(getCredits());
	}

	private static String getCredits()
	{
		return Core.getService(RpgLibrary.class).openResourceContents("credits.txt");
	}

	public LoginState(String userMessage)
	{
		//m_backgroundMusic = new Audio("audio/da/da.ogg");

		final UIStyle style = Core.getService(Game.class).getGameStyle();
		
		m_loginWindow = new Window(style, 500, 400);
		m_loginWindow.setRenderBackground(false);
		m_loginWindow.setMovable(false);
		m_loginWindow.setLocation(new Vector2D(262, 300));

		m_connectingAbout = new TextArea(Color.white, 480, 230);

		m_connectingAbout.setLocation(new Vector2D(10, 10));
		m_connectingAbout.setStyle(style);

		m_connectingAbout.setText(userMessage);

		m_loginWindow.addControl(m_connectingAbout);

		m_nickname = new TextArea(Color.white, 200, 80);
		m_nickname.setEditable(true);
		m_nickname.setText("Guest");
		m_nickname.setRenderBackground(false);

		m_loginWindow.addControl(new Label("Nickname:", Color.red), new Vector2D(170, 270));
		m_loginWindow.addControl(m_nickname, new Vector2D(220, 270));

		m_loginWindow.addControl(new Button("Connect!")
		{

			@Override
			public void onButtonPress()
			{
				try
				{
					ClientConfiguration config = m_context.getConfiguration();
					
					m_nickname.setText(m_nickname.getText().trim());

					Socket clientSocket = new Socket(config.server, config.port);
					clientSocket.setTcpNoDelay(true);

					ClientCommunicator communicator = m_context.getCommunicator();

					if (communicator.isBound())
						communicator.unbind();

					communicator.setUserCredentials(new UserCredentials(m_nickname.getText()));
					communicator.bind(new RemoteSocketCommunicator(clientSocket));

					m_context.setState(new LoadingState());

				} catch (IOException e)
				{
					m_connectingAbout.setText("The servers are currently inaccessible.\n\nTry again later");
				}
			}
		}, new Vector2D(200, 300));

		final Image overlayImage;

		try
		{
			overlayImage = ImageIO.read(Core.getService(IResourceLibrary.class).openAsset("ui/menu.png"));
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}

		m_overlayWindow = new Window(style, 1024, 768);
		m_overlayWindow.setRenderBackground(false);
		m_overlayWindow.setMovable(false);
		m_overlayWindow.setFocusable(false);

		Viewport view = new Viewport(new IRenderable()
		{
			@Override
			public void render(Graphics2D g, int x, int y, float fScale)
			{
				g.drawImage(overlayImage, x, y, null);
			}
		}, 1024, 768);

		view.setRenderBackground(false);

		m_overlayWindow.addControl(view);

		//m_backgroundMusic = new Audio("audio/da/da.ogg");

		m_menuWorld = World.create(Core.getService(IResourceLibrary.class).openConfiguration("world/menu.jmp"));

		Vector2D resolution = Core.getService(Game.class).getResolution();

		ControlledCamera menuCamera = new ControlledCamera();
		menuCamera.attach(m_menuWorld);
		menuCamera.lookAt(new Vector2F(9, 9));

		WorldView worldViewport = new WorldView(resolution.x, resolution.y);
		worldViewport.setRenderBackground(false);
		worldViewport.setCamera(menuCamera);

		m_worldViewWindow = new Window(style, resolution.x, resolution.y);
		m_worldViewWindow.setRenderBackground(false);
		m_worldViewWindow.setMovable(false);
		m_worldViewWindow.setFocusable(false);

		m_worldViewWindow.addControl(worldViewport);
	}

	@Override
	public void enter(ClientGame context)
	{
		m_context = context;
		//m_backgroundMusic.repeat();

		Core.getService(IWindowManager.class).addWindow(m_loginWindow);
		Core.getService(IWindowManager.class).addWindow(m_overlayWindow);
		Core.getService(IWindowManager.class).addWindow(m_worldViewWindow);
	}

	@Override
	public void leave()
	{
		//m_backgroundMusic.stop();

		Core.getService(IWindowManager.class).removeWindow(m_worldViewWindow);
		Core.getService(IWindowManager.class).removeWindow(m_loginWindow);
		Core.getService(IWindowManager.class).removeWindow(m_overlayWindow);

		m_menuWorld.dispose();
		m_context = null;
	}

	@Override
	public void update(int deltaTime)
	{
		m_menuWorld.update(deltaTime);
	}
}
