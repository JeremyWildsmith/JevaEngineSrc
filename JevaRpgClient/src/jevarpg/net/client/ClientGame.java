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

import proguard.annotation.KeepClassMemberNames;

import com.sun.istack.internal.Nullable;

import jeva.game.ICamera;
import jeva.game.IGameScriptProvider;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.world.Entity;
import jeva.world.World;
import jeva.world.World.WorldScriptContext;
import jevarpg.RpgCharacter;
import jevarpg.RpgGame;

public class ClientGame extends RpgGame
{
	private ClientCommunicator m_communicator = new ClientCommunicator();

	@Nullable
	private RpgCharacter m_player;

	@Nullable
	private ICamera m_camera;

	private IGameState m_state;
	
	private World m_world;

	@Override
	protected void startup()
	{
		super.startup();

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

		if (m_world != null)
			m_world.update(deltaTime);

		super.update(deltaTime);
	}

	public void setPlayer(@Nullable RpgCharacter player)
	{
		m_player = player;
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
	public void mouseButtonStateChanged(InputMouseEvent e) { }
	
	public class ClientGameScriptProvider extends RpgGameScriptProvider
	{
		@Override
		public Object getGameBridge()
		{
			return new ClientGameBridge();
		}

		@KeepClassMemberNames
		public class ClientGameBridge extends GameBridge
		{
			public WorldScriptContext getWorld()
    		{
				Entity entity = ClientGame.this.getPlayer();
		    	if(entity == null || !entity.isAssociated())
		    		return null;
		    	
		        return entity.getWorld().getScriptBridge();
		    }
		}
	}
}
