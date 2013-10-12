package jevarpg.net.server;

import java.nio.InvalidMarkException;

import jeva.communication.Communicator;
import jeva.communication.SharePolicy;
import jeva.communication.SharedClass;
import jeva.communication.SharedEntity;
import jevarpg.net.NetUser;

@SharedClass(name = "User", policy = SharePolicy.ClientR)
public class ServerUser extends NetUser implements IServerShared
{
	private ServerCommunicator m_server;

	private boolean m_isAuthenticated = false;

	private String m_username;

	private boolean m_dispatchedAuthenticationQuery = false;

	private IUserHandler m_handler;

	private int m_ping = 0;

	private int m_pingDispatch = 0;

	public ServerUser(IUserHandler handler, ServerCommunicator server)
	{
		m_handler = handler;
		m_server = server;
	}

	@Override
	public SharedEntity getSharedEntity()
	{
		return this;
	}

	public boolean isAuthenticated()
	{
		return m_isAuthenticated;
	}

	public String getUsername()
	{
		return m_username;
	}

	public void assignEntity(String name)
	{
		send(new CharacterAssignment(name));
	}

	public void unassignEntity()
	{
		send(new CharacterAssignment());
	}

	public void sendChatMessage(String username, String message)
	{
		send(new ChatMessage(username, message));
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMarkException
	{
		if (recv instanceof PrimitiveQuery)
		{
			if (((PrimitiveQuery) recv) == PrimitiveQuery.Ping)
				m_ping = 0;
		} else if (recv instanceof AuthenticationQuery)
		{
			AuthenticationQuery query = (AuthenticationQuery) recv;

			if (m_handler.loginUser(query.getCredentials()))
			{
				m_isAuthenticated = true;
				m_username = query.getCredentials().getNickname();
				send(PrimitiveQuery.Authenticated);
			} else
				send(PrimitiveQuery.AuthenticationFailed);

		} else if (recv instanceof ChatMessage)
		{
			if (!isAuthenticated())
				return false;

			m_handler.recieveChatMessage(((ChatMessage) recv).getMessage());

		} else
			m_server.disconnect("Invalid message recieved from client");

		return true;
	}

	@Override
	public void update(int deltaTime)
	{
		if (!m_dispatchedAuthenticationQuery)
		{
			m_dispatchedAuthenticationQuery = true;
			send(PrimitiveQuery.QueryAuthentication);
		}

		m_pingDispatch += deltaTime;
		m_ping += deltaTime;

		if (m_pingDispatch >= PING_INTERVAL)
		{
			m_pingDispatch = 0;
			send(PrimitiveQuery.Ping);
			snapshot();
		}

		if (m_ping >= PING_TIMEOUT)
		{
			m_ping = 0;
			m_server.disconnect("Client Timed Out");
		}

		snapshot();
	}

	public interface IUserHandler
	{
		boolean loginUser(UserCredentials credentials);

		void recieveChatMessage(String message);
	}
}
