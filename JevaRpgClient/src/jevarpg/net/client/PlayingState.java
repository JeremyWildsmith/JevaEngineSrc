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

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.game.FollowCamera;
import jeva.graphics.ui.Button;
import jeva.graphics.ui.UIStyle;
import jeva.graphics.ui.Window;
import jeva.graphics.ui.IWindowManager;
import jeva.math.Vector2D;
import jeva.world.Entity;
import jeva.world.World;
import jeva.world.World.IWorldObserver;
import jevarpg.RpgCharacter;
import jevarpg.net.client.ClientCommunicator.IClientCommunicatorObserver;
import jevarpg.net.client.ClientUser.IClientUserObserver;
import jevarpg.net.client.ui.ChatMenu;
import jevarpg.ui.CharacterMenu;
import jevarpg.ui.InventoryMenu;

public class PlayingState implements IGameState
{
	private ClientGame m_context;

	private Window m_hud;

	private ChatMenu m_chatMenu;

	private EventHandler m_handler = new EventHandler();

	private World m_world;
	
	private ClientUser m_user;
	
	private FollowCamera m_camera = new FollowCamera();

	private InventoryMenu m_inventoryMenu = new InventoryMenu();
	private CharacterMenu m_characterMenu = new CharacterMenu();
	
	@Nullable
	private String m_playerEntityName;

	public PlayingState(String playerEntityName, ClientUser user, World world)
	{
		final UIStyle styleLarge = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/large.juis")));
		final UIStyle styleSmall = UIStyle.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream("ui/tech/small.juis")));

		m_playerEntityName = playerEntityName;
		m_user = user;
		m_world = world;

		m_hud = new Window(styleLarge, 177, 80);
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

		m_chatMenu = new ChatMenu(styleSmall)
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

		m_user.addObserver(m_handler);
		context.getCommunicator().addObserver(m_handler);

		m_context.setWorld(m_world);
		
		m_context.setCamera(m_camera);

		if (m_playerEntityName != null && m_world.variableExists(m_playerEntityName))
		{
			Variable characterVar = m_world.getVariable(m_playerEntityName);

			if (!(characterVar instanceof RpgCharacter))
				m_context.getCommunicator().disconnect("Server did not assign proper character entity");

			playerAdded((RpgCharacter) characterVar);
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
		
		m_user.removeObserver(m_handler);
		m_world.removeObserver(m_handler);
		m_context.getCommunicator().removeObserver(m_handler);

		m_context.clearCamera();
		
		m_context.setPlayer(null);
		m_context.clearWorld();

		m_context = null;
	}

	@Override
	public void update(int deltaTime) { }

	private void playerAdded(RpgCharacter player)
	{
		m_camera.setTarget(player.getName());
		m_context.setPlayer(player);
		m_hud.setVisible(true);
	}

	private void playerRemoved()
	{
		m_context.setPlayer(null);
		m_hud.setVisible(false);
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
			if (m_playerEntityName != null && e instanceof RpgCharacter && e.getName().equals(m_playerEntityName))
				playerAdded((RpgCharacter) e);
		}

		@Override
		public void removedEntity(Entity e)
		{
			if (m_playerEntityName != null && e.getName().equals(m_playerEntityName))
				playerRemoved();
		}
	}
}
