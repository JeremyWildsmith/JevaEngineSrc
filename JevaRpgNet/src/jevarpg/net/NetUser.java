package jevarpg.net;

import com.sun.istack.internal.Nullable;

import jeva.communication.SharedEntity;

public abstract class NetUser extends SharedEntity
{

	protected static final int PING_TIMEOUT = 15000;
	protected static final int PING_INTERVAL = 3000;

	public static class UserCredentials
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

	protected static enum PrimitiveQuery
	{
		Ping, AuthenticationFailed, QueryAuthentication, Authenticated,
	}

	protected static class CharacterAssignment
	{
		@Nullable
		private String m_entityName;

		public CharacterAssignment()
		{
		}

		public CharacterAssignment(String entityName)
		{
			m_entityName = entityName;
		}

		public boolean isUnassignment()
		{
			return m_entityName == null;
		}

		public String getEntityName()
		{
			if (m_entityName == null)
				throw new IllegalStateException("Cannot retrive name of entity player is not being assigned to.");

			return m_entityName;
		}
	}

	protected static class ChatMessage
	{
		@Nullable
		private String m_user;
		private String m_sMessage;

		@SuppressWarnings("unused")
		// Used by Kryo
		private ChatMessage()
		{
		}

		public ChatMessage(@Nullable String user, String message)
		{
			m_user = user;
			m_sMessage = message;
		}

		public ChatMessage(String message)
		{
			this(null, message);
		}

		@Nullable
		public String getUser()
		{
			return m_user;
		}

		public String getMessage()
		{
			return m_sMessage;
		}
	}

	protected static class AuthenticationQuery
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
}
