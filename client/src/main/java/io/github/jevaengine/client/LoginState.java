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

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.client.ClientGame.ClientConfiguration;
import io.github.jevaengine.communication.tcp.RemoteSocketCommunicator;
import io.github.jevaengine.netcommon.user.UserCredentials;

import java.io.IOException;
import java.net.Socket;

public abstract class LoginState implements IClientGameState
{
	private ClientConfiguration m_configuration;
	
	protected final void connect(String nickname) throws ConnectionFailedException
	{
		try(Socket clientSocket = new Socket(m_configuration.server, m_configuration.port))
		{
			ClientGame game = Core.getService(ClientGame.class);
			clientSocket.setTcpNoDelay(true);
			
			game.connect(new RemoteSocketCommunicator(clientSocket));
			game.setState(new WaitForUserState(new UserCredentials(nickname)));

		} catch (IOException e)
		{
			throw new ConnectionFailedException(e);
		}
	}
	
	@Override
	public final void enter()
	{
		m_configuration = Core.getService(ResourceLibrary.class).openConfiguration("client.cfg").getValue(ClientConfiguration.class);
		onEnter();
	}

	@Override
	public final void leave()
	{
		onLeave();
	}

	protected abstract void onEnter();
	protected abstract void onLeave();
	
	public static class ConnectionFailedException extends Exception
	{
		private static final long serialVersionUID = 1L;

		private ConnectionFailedException(Exception cause)
		{
			super(cause);
		}
	}
}
