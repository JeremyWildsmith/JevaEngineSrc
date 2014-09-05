package io.github.jevaengine.client;

import io.github.jevaengine.Core;

public abstract class DisconnectedState implements IClientGameState
{	
	@Override
	public void enter()
	{
		onEnter();
	}

	@Override
	public void leave()
	{
		onLeave();
	}
	
	protected final void returnToLogin()
	{
		ClientGame game = Core.getService(ClientGame.class);
		game.setState(game.getStateFactory().createLoginState());
	}
	
	protected abstract void onEnter();
	protected abstract void onLeave();
}
