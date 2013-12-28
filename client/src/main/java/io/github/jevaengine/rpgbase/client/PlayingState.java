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
import io.github.jevaengine.game.FollowCamera;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.client.ClientCommunicator.IClientCommunicatorObserver;
import io.github.jevaengine.rpgbase.client.ClientUser.IClientUserObserver;
import io.github.jevaengine.rpgbase.client.ui.ChatMenu;
import io.github.jevaengine.rpgbase.ui.CharacterMenu;
import io.github.jevaengine.rpgbase.ui.InventoryMenu;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.MenuStrip;
import io.github.jevaengine.ui.MenuStrip.IMenuStripListener;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.ui.WorldView.IWorldViewListener;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.IInteractable;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.IWorldObserver;

import java.awt.Color;

public class PlayingState implements IGameState
{
	private ClientGame m_context;

	private EventHandler m_handler = new EventHandler();

	private World m_world;

	private ClientUser m_user;

	@Nullable private RpgCharacter m_playerCharacter;

	@Nullable private String m_playerEntityName;

	private FollowCamera m_playerCamera = new FollowCamera();

	private Window m_worldViewWindow;
	private Window m_hud;
	private ChatMenu m_chatMenu;
	private InventoryMenu m_inventoryMenu = new InventoryMenu();
	private CharacterMenu m_characterMenu = new CharacterMenu();

	private MenuStrip m_contextStrip = new MenuStrip();
	
	private Label m_cursorActionLabel = new Label("", Color.yellow);
	private WorldView m_worldView;

	public PlayingState(String playerEntityName, ClientUser user, World world)
	{
		final UIStyle style = Core.getService(Game.class).getGameStyle();
		
		m_playerEntityName = playerEntityName;
		m_user = user;
		m_world = world;

		m_hud = new Window(style, 177, 80);
		m_hud.addControl(new Button("Inventory")
		{

			@Override
			public void onButtonPress()
			{
				m_inventoryMenu.accessInventory(m_context.getPlayer().getInventory(), m_context.getPlayer());
			}
		}, new Vector2D(5, 10));

		m_hud.addControl(new Button("Character")
		{
			@Override
			public void onButtonPress()
			{
				m_characterMenu.showCharacter(m_context.getPlayer());
			}
		}, new Vector2D(5, 40));

		m_chatMenu = new ChatMenu(style)
		{

			@Override
			public void onSend(String message)
			{
				m_user.sendChat(message);
			}
		};

		m_chatMenu.setLocation(new Vector2D(560, 570));
		m_chatMenu.setMovable(false);

		m_hud.setLocation(new Vector2D(20, 670));
		m_hud.setMovable(false);
		m_hud.setVisible(false);

		Vector2D resolution = Core.getService(Game.class).getResolution();

		m_playerCamera = new FollowCamera();

		m_worldView = new WorldView(resolution.x, resolution.y);
		m_worldView.setRenderBackground(false);
		m_worldView.setCamera(m_playerCamera);
		m_worldView.addListener(new WorldViewListener());
		m_worldView.addControl(m_cursorActionLabel);

		m_worldViewWindow = new Window(style, resolution.x, resolution.y);
		m_worldViewWindow.setRenderBackground(false);
		m_worldViewWindow.setMovable(false);
		m_worldViewWindow.setFocusable(false);

		m_worldViewWindow.addControl(m_worldView);
		m_worldViewWindow.addControl(m_contextStrip);
	}

	@Override
	public void enter(ClientGame context)
	{

		m_context = context;

		final IWindowManager windowManager = Core.getService(IWindowManager.class);

		windowManager.addWindow(m_hud);
		windowManager.addWindow(m_chatMenu);
		windowManager.addWindow(m_inventoryMenu);
		windowManager.addWindow(m_characterMenu);
		windowManager.addWindow(m_worldViewWindow);

		m_user.addObserver(m_handler);
		context.getCommunicator().addObserver(m_handler);

		Entity playerEntity = m_playerEntityName == null ? null : m_world.getEntity(m_playerEntityName);
		
		if (playerEntity != null)
		{
			if (playerEntity instanceof RpgCharacter)
				playerAdded((RpgCharacter) playerEntity);
			else
				m_context.getCommunicator().disconnect("Server did not assign proper character entity");
		}

		m_world.addObserver(m_handler);
	}

	@Override
	public void leave()
	{
		final IWindowManager windowManager = Core.getService(IWindowManager.class);

		windowManager.removeWindow(m_hud);
		windowManager.removeWindow(m_chatMenu);
		windowManager.removeWindow(m_inventoryMenu);
		windowManager.removeWindow(m_characterMenu);
		windowManager.removeWindow(m_worldViewWindow);

		m_user.removeObserver(m_handler);
		m_world.removeObserver(m_handler);
		m_context.getCommunicator().removeObserver(m_handler);

		m_context.setPlayer(null);

		m_context = null;
	}

	@Override
	public void update(int deltaTime)
	{
		m_world.update(deltaTime);
	}

	private void playerAdded(RpgCharacter player)
	{
		m_playerCharacter = player;
		m_playerCamera.attach(m_world);
		m_playerCamera.setTarget(player.getInstanceName());
		m_context.setPlayer(player);
		m_hud.setVisible(true);
	}

	private void playerRemoved()
	{
		m_playerCharacter = null;
		m_playerCamera.dettach();
		m_context.setPlayer(null);
		m_hud.setVisible(false);
	}

	private class WorldViewListener implements IWorldViewListener
	{
		private IInteractable m_lastTarget;
		
		@Override
		public void worldSelection(Vector2D screenLocation, Vector2F worldLocation, MouseButton button)
		{
			final IInteractable[] interactables = m_world.getTileEffects(worldLocation.round()).interactables.toArray(new IInteractable[0]);

			if (button == MouseButton.Left)
			{
				if(m_lastTarget != null)
				{
					m_lastTarget.doCommand(m_lastTarget.getDefaultCommand());
					m_lastTarget = null;
					m_cursorActionLabel.setVisible(false);
				}else if (m_playerCharacter != null)
					m_playerCharacter.moveTo(worldLocation);

				m_contextStrip.setVisible(false);
			} else if (button == MouseButton.Right)
			{
				if (interactables.length > 0 && interactables[0].getCommands().length > 0)
				{
					m_contextStrip.setContext(interactables[0].getCommands(), new IMenuStripListener()
					{
						@Override
						public void onCommand(String command)
						{
							interactables[0].doCommand(command);
						}
					});

					m_contextStrip.setLocation(screenLocation.difference(m_contextStrip.getParent().getAbsoluteLocation()));
				}
			}
		}

		@Override
		public void worldMove(Vector2D screenLocation, Vector2F worldLocation)
		{
			final IInteractable[] interactables = m_world.getTileEffects(worldLocation.round()).interactables.toArray(new IInteractable[0]);
			
			IInteractable defaultable = null;
			
			for(int i = 0; i < interactables.length && defaultable == null; i++)
			{
				if(interactables[i].getDefaultCommand() != null)
					defaultable = interactables[i];
			}
			
			if(defaultable != null)
			{
				m_cursorActionLabel.setText(defaultable.getDefaultCommand());
				m_cursorActionLabel.setVisible(true);
				
				Vector2D offset = new Vector2D(10, 15);
				
				m_cursorActionLabel.setLocation(screenLocation.difference(PlayingState.this.m_worldView.getAbsoluteLocation()).add(offset));
			
				m_lastTarget = defaultable;
			}else
			{
				m_lastTarget = null;
				m_cursorActionLabel.setVisible(false);
			}
		}

	}

	private class EventHandler implements IClientUserObserver, IClientCommunicatorObserver, IWorldObserver
	{
		@Override
		public void timeout()
		{
			m_context.getCommunicator().disconnect("Timeout");
		}

		@Override
		public void recieveChatMessage(String user, String message)
		{
			m_chatMenu.recieveChatMessage(user, message);
		}

		@Override
		public void assignedPlayer(String entityName)
		{
			m_playerEntityName = entityName;
		}

		@Override
		public void unassignedPlayer()
		{
			m_context.setPlayer(null);
			m_playerEntityName = null;
		}

		@Override
		public void unservedWorld()
		{
			m_context.setState(new LoadingState(m_user, m_playerEntityName));
		}

		@Override
		public void disconnected(String cause)
		{
			m_context.setState(new LoginState(cause));
		}

		// By the time we've reached this state, the player is fully
		// authenticated.
		@Override
		public void authenticated()
		{
		}

		@Override
		public void authenticationFailed()
		{
		}

		@Override
		public void servedUser(ClientUser user)
		{
		}

		@Override
		public void unservedUser()
		{
		}

		@Override
		public void servedWorld(World world)
		{
		}

		@Override
		public void addedEntity(Entity e)
		{
			if (m_playerEntityName != null && e instanceof RpgCharacter && e.getInstanceName().equals(m_playerEntityName))
				playerAdded((RpgCharacter) e);
		}

		@Override
		public void removedEntity(Entity e)
		{
			if (m_playerEntityName != null && e.getInstanceName().equals(m_playerEntityName))
				playerRemoved();
		}
	}
}
