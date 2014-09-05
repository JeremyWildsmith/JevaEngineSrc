package io.github.jevaengine.netcommon.user;

public class UserCredentials
{
	private String m_nickname;

	@SuppressWarnings("unused")
	// Used by Kryo
	private UserCredentials()
	{
	}

	public UserCredentials(String nickname)
	{
		m_nickname = nickname;
	}

	public String getNickname()
	{
		return m_nickname;
	}
}