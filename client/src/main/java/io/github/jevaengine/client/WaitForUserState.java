package io.github.jevaengine.client;

import io.github.jevaengine.Core;
import io.github.jevaengine.netcommon.user.UserCredentials;

public final class WaitForUserState implements IClientGameState
{
	private UserCredentials m_credentials;
	
	private static final int TIMEOUT = 8000;
	
	private int m_elapsedTime = 0;
	
	public WaitForUserState(UserCredentials credentials)
	{
		m_credentials = credentials;
	}
	
	@Override
	public void enter() { }

	@Override
	public void leave() { }

	@Override
	public void update(int deltaTime)
	{
		ClientGame game = Core.getService(ClientGame.class);
		if(game.getUser() != null)
			game.setState(game.getStateFactory().createAuthenticatingState(m_credentials));
	
		m_elapsedTime += deltaTime;
		
		if(m_elapsedTime >= TIMEOUT)
			game.disconnect("Timed out waiting for server to share user object.");
	}
}
