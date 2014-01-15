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
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.game.IGameScriptProvider;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.RpgGame;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World.WorldScriptContext;

public class ClientGame extends RpgGame
{
	private ClientCommunicator m_communicator = new ClientCommunicator();

	@Nullable private RpgCharacter m_player;

	private IGameState m_state;

	private UIStyle m_style;
	private Sprite m_cursor;
	
	private ClientConfiguration m_configuration;
	
	@Override
	protected void startup()
	{
		super.startup();

		ResourceLibrary library = Core.getService(ResourceLibrary.class);
		
		m_configuration = library.openConfiguration("client.cfg").getValue(ClientConfiguration.class);
		
		m_style = UIStyle.create(library.openConfiguration("ui/game.juis"));
		m_cursor = Sprite.create(library.openConfiguration("ui/tech/cursor/cursor.jsf"));
		m_cursor.setAnimation("idle", AnimationState.Play);
		
		setState(new LoginState());
	}

	public ClientCommunicator getCommunicator()
	{
		return m_communicator;
	}

	protected void setState(IGameState state)
	{
		if (m_state != null)
			m_state.leave();

		m_state = state;
		state.enter(this);
	}

	@Override
	public void update(int deltaTime)
	{
		m_state.update(deltaTime);
		m_communicator.update(deltaTime);

		super.update(deltaTime);
	}

	protected void setPlayer(@Nullable RpgCharacter player)
	{
		m_player = player;
	}

	public ClientConfiguration getConfiguration()
	{
		return m_configuration;
	}
	
	@Override
	public RpgCharacter getPlayer()
	{
		return m_player;
	}

	@Override
	public IGameScriptProvider getScriptBridge()
	{
		return new ClientGameScriptProvider();
	}

	@Override
	public void mouseButtonStateChanged(InputMouseEvent e)
	{
	}

	@Override
	public UIStyle getGameStyle()
	{
		return m_style;
	}

	@Override
	protected Sprite getCursor()
	{
		return m_cursor;
	}

	public static class ClientConfiguration implements ISerializable
	{
		public String server;
		public int port;
		
		public ClientConfiguration() { }

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("server").setValue(this.server);
			target.addChild("port").setValue(this.port);
		}

		@Override
		public void deserialize(IImmutableVariable source)
		{
			server = source.getChild("server").getValue(String.class);
			port = source.getChild("port").getValue(Integer.class);
		}
	}
	
	public class ClientGameScriptProvider extends RpgGameScriptProvider
	{
		@Override
		public Object getGameBridge()
		{
			return new ClientGameBridge();
		}

		public class ClientGameBridge extends GameBridge
		{
			public WorldScriptContext getWorld()
			{
				Entity entity = ClientGame.this.getPlayer();
				if (entity == null || !entity.isAssociated())
					return null;

				return entity.getWorld().getScriptBridge();
			}
		}
	}
}
