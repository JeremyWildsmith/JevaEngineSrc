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
package io.github.jevaengine.client;

import io.github.jevaengine.Core;

public abstract class PlayingState implements IClientGameState
{
	private ClientWorld m_world;
	
	public PlayingState(ClientWorld world)
	{
		m_world = world;
	}
	
	@Override
	public final void enter()
	{
		onEnter();
	}

	@Override
	public final void leave()
	{
		onLeave();
	}

	@Override
	public void update(int deltaTime)
	{
		ClientGame game = Core.getService(ClientGame.class);
		
		if(!game.getUser().isSharing(m_world))
			game.setState(game.getStateFactory().createSelectWorldState());
	}
	
	protected final ClientWorld getClientWorld()
	{
		return m_world;
	}
	
	protected abstract void onEnter();
	protected abstract void onLeave();
}
