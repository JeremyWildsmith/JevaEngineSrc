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

import jeva.game.ControlledCamera;
import jeva.game.IWorldCamera;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.math.Vector2D;
import jevarpg.RpgCharacter;
import jevarpg.RpgGame;

public class ClientGame extends RpgGame
{

	private ClientCommunicator m_communicator = new ClientCommunicator();

	@Nullable
	private RpgCharacter m_player;

	private final ControlledCamera m_defaultCamera = new ControlledCamera();

	@Nullable
	private IWorldCamera m_camera;

	private IGameState m_state;

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

		if (getWorld() != null)
			getWorld().update(deltaTime);

		super.update(deltaTime);
	}

	@Override
	protected IWorldCamera getCamera()
	{
		return m_camera == null ? m_defaultCamera : m_camera;
	}

	public void setCamera(IWorldCamera camera)
	{
		m_camera = camera;
		m_camera.attach(getWorld());
	}

	public void clearCamera()
	{
		m_camera.dettach();
		m_camera = null;
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
	protected void worldSelection(InputMouseEvent e, Vector2D location)
	{
		super.worldSelection(e, location);
	}

	@Override
	public void mouseButtonStateChanged(InputMouseEvent e) { }

}
