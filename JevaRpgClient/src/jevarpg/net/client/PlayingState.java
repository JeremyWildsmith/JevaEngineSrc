package jevarpg.net.client;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.graphics.ui.Button;
import jeva.graphics.ui.UIStyle;
import jeva.graphics.ui.Window;
import jeva.math.Vector2D;
import jeva.world.Entity;
import jeva.world.World;
import jeva.world.World.IWorldObserver;
import jevarpg.RpgCharacter;
import jevarpg.net.client.ClientCommunicator.IClientCommunicatorObserver;
import jevarpg.net.client.ClientUser.IClientUserObserver;
import jevarpg.net.client.ui.ChatMenu;

public class PlayingState implements IGameState
{
	private ClientGame m_context;

	private Window m_hud;

	private ChatMenu m_chatMenu;

	private EventHandler m_handler = new EventHandler();

	private World m_world;
	private ClientUser m_user;

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
				m_context.getInventoryMenu().accessInventory(m_context.getPlayer().getInventory(), m_context.getPlayer());
			}
		}, new Vector2D(5, 10));

		m_hud.addControl(new Button("Character")
		{

			@Override
			public void onButtonPress()
			{
				m_context.getCharacterMenu().showCharacter(m_context.getPlayer());
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
		m_context.getWindowManager().addWindow(m_hud);
		m_context.getWindowManager().addWindow(m_chatMenu);

		m_user.addListener(m_handler);

		m_context.setWorld(m_world);

		if (m_playerEntityName != null && m_world.variableExists(m_playerEntityName))
		{
			Variable characterVar = m_world.getVariable(m_playerEntityName);

			if (!(characterVar instanceof RpgCharacter))
				m_context.getCommunicator().disconnect("Server did not assign proper character entity");

			playerAdded((RpgCharacter) characterVar);
		}

		m_world.addListener(m_handler);
	}

	@Override
	public void leave()
	{
		m_context.getWindowManager().removeWindow(m_hud);
		m_context.getWindowManager().removeWindow(m_chatMenu);

		m_user.removeListener(m_handler);
		m_world.removeListener(m_handler);

		m_context.setPlayer(null);
		m_context.clearWorld();

		m_context = null;
	}

	@Override
	public void update(int deltaTime)
	{
	}

	private void playerAdded(RpgCharacter player)
	{
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
			m_context.setState(new LoadingState());
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
			if (m_playerEntityName == null && e instanceof RpgCharacter && e.getName().equals(m_playerEntityName))
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
