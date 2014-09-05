package io.github.jevaengine.netcommon.user;

public class AuthenticationQuery
{
	private UserCredentials m_credentials;

	@SuppressWarnings("unused")
	// Used by Kryo
	private AuthenticationQuery()
	{
	}

	public AuthenticationQuery(UserCredentials credentials)
	{
		m_credentials = credentials;
	}

	public UserCredentials getCredentials()
	{
		return m_credentials;
	}
}