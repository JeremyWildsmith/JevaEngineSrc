package io.github.jevaengine.client;

import io.github.jevaengine.Core;

public abstract class SelectWorldState implements IClientGameState
{
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
	
	protected final void selectWorld(String worldName)
	{
		ClientGame game = Core.getService(ClientGame.class);
		game.setState(game.getStateFactory().createLoadingWorldState(worldName));
	}
	
	protected abstract void onEnter();
	protected abstract void onLeave();
}
