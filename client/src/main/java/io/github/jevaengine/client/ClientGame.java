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

import io.github.jevaengine.client.ClientUser.IClientUserTimeoutObserver;
import io.github.jevaengine.communication.Communicator.RemoteCommunicator;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.game.DefaultGame;
import io.github.jevaengine.util.Nullable;

public abstract class ClientGame extends DefaultGame
{
	private ClientCommunicator m_communicator = new ClientCommunicator();

	private IClientGameState m_state;
	
	@Nullable
	private ClientUser m_clientUser;
	
	private ClientUserTimeoutObserver m_clientUserTimeoutObserver = new ClientUserTimeoutObserver();
	
	@Override
	protected void startup()
	{
		setState(getStateFactory().createLoginState());
	}
	
	private void checkClientUser()
	{
		ClientUser currentUser = m_communicator.getUser();
		
		if(m_clientUser != currentUser)
		{
			if(m_clientUser != null)
			{
				m_clientUser.removeTimeoutObserver(m_clientUserTimeoutObserver);
				disconnect("Client user changed during active session.");
			}
			else
			{
				if(currentUser != null)
					currentUser.addTimeoutObserver(m_clientUserTimeoutObserver);
			}
			
			m_clientUser = currentUser;
		}
	}
	
	@Override
	public boolean update(int deltaTime)
	{
		checkClientUser();
		m_state.update(deltaTime);
		m_communicator.update(deltaTime);

		return super.update(deltaTime);
	}
			
	final void connect(RemoteCommunicator remote)
	{
		if(m_communicator.isBound())
			throw new ClientAlreadyConnectedException();
		else
			m_communicator.bind(remote);
	}

	final void disconnect(String reason)
	{
		if(!m_communicator.isBound())
			throw new ClientNotConnectedException();
			
		m_communicator.unbind();
		setState(getStateFactory().createDisconnectedState(reason));
	}
		
	final void setState(IClientGameState state)
	{
		if (m_state != null)
			m_state.leave();

		m_state = state;
		state.enter();
	}
		
	public ClientUser getUser()
	{
		return m_clientUser;
	}
		
	public abstract IClientGameStateFactory getStateFactory();
		
	private static class ClientAlreadyConnectedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	
		private ClientAlreadyConnectedException() { }
	}
	
	private static class ClientNotConnectedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
	
		private ClientNotConnectedException() { }
	}
	
	private class ClientUserTimeoutObserver implements IClientUserTimeoutObserver
	{
		@Override
		public void timeout()
		{
			disconnect("Timed out");
		}
	}
	
	public static class ClientConfiguration implements ISerializable
	{
		public String server;
		public int port;
		
		public ClientConfiguration() { }

		@Override
		public void deserialize(IImmutableVariable source)
		{
			server = source.getChild("server").getValue(String.class);
			port = source.getChild("port").getValue(Integer.class);
		}

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("server").setValue(this.server);
			target.addChild("port").setValue(this.port);
		}
	}
}
