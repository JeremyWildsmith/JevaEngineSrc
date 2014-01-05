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
package io.github.jevaengine.rpgbase.client;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.rpgbase.netcommon.NetUser;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;

@SharedClass(name = "User", policy = SharePolicy.ClientR)
public final class ClientUser extends NetUser
{
	private UserCredentials m_credentials;

	private int m_ping = 0;

	private int m_pingDispatch = 0;

	private boolean m_isAuthenticated = false;

	private Observers m_observers = new Observers();

	@Nullable private String m_assignedPlayerEntity = null;

	public ClientUser(UserCredentials credentials)
	{
		m_credentials = credentials;
	}

	public void addObserver(IClientUserObserver o)
	{
		m_observers.add(o);
	}

	public void removeObserver(IClientUserObserver o)
	{
		m_observers.remove(o);
	}

	public boolean isAuthenticated()
	{
		return m_isAuthenticated;
	}

	public boolean isAssignedCharacter()
	{
		return m_assignedPlayerEntity != null;
	}

	public String getAssignedCharacter()
	{
		if (m_assignedPlayerEntity == null)
			throw new IllegalStateException("User has not been assigned a character");

		return m_assignedPlayerEntity;
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if (recv instanceof PrimitiveQuery)
		{
			switch ((PrimitiveQuery) recv)
			{
				case Ping:
					m_ping = 0;
					break;
				case Authenticated:
					m_isAuthenticated = true;
					m_observers.authenticated();
					break;
				case AuthenticationFailed:
					m_isAuthenticated = false;
					m_observers.authenticationFailed();
					break;
				case QueryAuthentication:
					send(new AuthenticationQuery(m_credentials));
					break;
				default:
					throw new InvalidMessageException(sender, recv, "Unrecognized query from server");
			}
		} else if (recv instanceof CharacterAssignment)
		{
			CharacterAssignment assignment = (CharacterAssignment) recv;

			if (assignment.isUnassignment())
			{
				if (m_assignedPlayerEntity != null)
					throw new InvalidMessageException(sender, recv, "Server attempted to unassign player entity when one was not yet assigned.");

				m_observers.unassignedPlayer();

				m_assignedPlayerEntity = null;
			} else
			{
				if (m_assignedPlayerEntity != null)
					throw new InvalidMessageException(sender, recv, "Server attempted to assign player entity when one was already assigned.");

				m_assignedPlayerEntity = assignment.getEntityName();

				m_observers.assignedPlayer(m_assignedPlayerEntity);
			}
		} else if (recv instanceof ChatMessage)
		{
			ChatMessage msg = (ChatMessage) recv;
			m_observers.recieveChatMessage(msg.getUser(), msg.getMessage());
		} else
			throw new InvalidMessageException(sender, recv, "Server attempted to unassign player entity when one was not yet assigned.");

		return true;
	}

	public void update(int deltaTime)
	{
		m_pingDispatch += deltaTime;
		m_ping += deltaTime;

		if (m_pingDispatch >= PING_INTERVAL)
		{
			m_pingDispatch = 0;
			send(PrimitiveQuery.Ping);
		}

		if (m_ping >= PING_TIMEOUT)
		{
			m_ping = 0;
			m_observers.timeout();
		}

		snapshot();
	}

	public void sendChat(String message)
	{
		send(new ChatMessage(message));
	}
	
	private static class Observers extends StaticSet<IClientUserObserver>
	{
		public void timeout()
		{
			for (IClientUserObserver o : this)
				o.timeout();
		}

		public void recieveChatMessage(String user, String message)
		{
			for (IClientUserObserver o : this)
				o.recieveChatMessage(user, message);
		}

		public void authenticated()
		{
			for (IClientUserObserver o : this)
				o.authenticated();
		}

		public void authenticationFailed()
		{
			for (IClientUserObserver o : this)
				o.authenticationFailed();
		}

		public void assignedPlayer(String entityName)
		{
			for (IClientUserObserver o : this)
				o.assignedPlayer(entityName);
		}

		public void unassignedPlayer()
		{
			for (IClientUserObserver o : this)
				o.unassignedPlayer();
		}
	}

	public interface IClientUserObserver
	{
		void timeout();

		void recieveChatMessage(String user, String message);

		void authenticated();

		void authenticationFailed();

		void assignedPlayer(String entityName);

		void unassignedPlayer();
	}
}
