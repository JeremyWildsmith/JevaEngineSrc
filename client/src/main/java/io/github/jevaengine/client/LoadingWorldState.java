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
import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.client.ClientUser.ShareWorldRequestDeclinedException;
import io.github.jevaengine.client.ClientUser.ShareWorldRequestTimedOutException;
import io.github.jevaengine.util.Nullable;

public abstract class LoadingWorldState implements IClientGameState
{
	//This variable can 'pop' into existence via another thread. But will not be mutated\accessed, just assigned initially once.
	@Nullable
	private volatile FutureResult<ClientWorld> m_loadedWorld;

	private String m_requestedWorld;
	private ClientUser m_user;
	
	public LoadingWorldState(String world)
	{
		m_requestedWorld = world;
	}

	private void loadingCompleted(ClientWorld world)
	{
		ClientGame game = Core.getService(ClientGame.class);
		
		game.setState(game.getStateFactory().createPlayingState(world));
	}
	
	private void loadingFailed(String cause)
	{
		ClientGame game = Core.getService(ClientGame.class);
		
		game.disconnect("Failed to load world: " + cause);
	}

	@Override
	public final void enter()
	{
		m_user = Core.getService(ClientGame.class).getUser();
		m_user.requestWorld(m_requestedWorld, getInitializationMonitor());
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
		if(m_loadedWorld != null)
		{
			try
			{
				loadingCompleted(m_loadedWorld.get());
			}catch(ShareWorldRequestDeclinedException e)
			{
				loadingFailed("World request was declined by server.");
			}catch(ShareWorldRequestTimedOutException e)
			{
				loadingFailed("World request time-out.");
			}catch(Exception e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public enum LoadWorldFailCause
	{
		Timeout,
		Declined,
	}
	
	protected abstract void onEnter();
	protected abstract void onLeave();
	protected abstract IInitializationMonitor<ClientWorld> getInitializationMonitor();
}
