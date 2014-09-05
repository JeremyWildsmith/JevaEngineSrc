package io.github.jevaengine.client;

import io.github.jevaengine.Core;
import io.github.jevaengine.client.ClientUser.AlreadyAuthenticatedException;
import io.github.jevaengine.client.ClientUser.BusyAuthenticatingException;
import io.github.jevaengine.client.ClientUser.IClientUserAuthenticationHandler;
import io.github.jevaengine.netcommon.user.UserCredentials;

public abstract class AuthenticatingState implements IClientGameState
{
	private ClientGame m_context;
	
	private UserCredentials m_credentials;
	
	public AuthenticatingState(UserCredentials credentials)
	{
		m_credentials = credentials;
	}
	
	@Override
	public final void enter()
	{
		m_context = Core.getService(ClientGame.class);
		
		final ClientUser user = m_context.getUser();
		
		try
		{
			user.queryAuthentication(m_credentials, new IClientUserAuthenticationHandler() {

				@Override
				public void authenticated()
				{
					if(user == m_context.getUser())
						m_context.setState(m_context.getStateFactory().createSelectWorldState());
				}

				@Override
				public void authenticationFailed()
																																																																																																																																																																																																																																																																																																																																																																																																																																													{
					if(user == m_context.getUser())
						m_context.disconnect("User authentication failed.");
				}

				@Override
				public void authenticationInvalidated()
				{
					if(user == m_context.getUser())
						m_context.disconnect("Authentication to server was invalidated.");
				}
			});
		} catch (AlreadyAuthenticatedException | BusyAuthenticatingException e)
		{
			m_context.disconnect("ClientUser recieved in unexpected state.");
		}

		onEnter();
	}

	@Override
	public final void leave()
	{
		onLeave();
		m_context = null;
	}
	
	protected abstract void onEnter();
	protected abstract void onLeave();
}
