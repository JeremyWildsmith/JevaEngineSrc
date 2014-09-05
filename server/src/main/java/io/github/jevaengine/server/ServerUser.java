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
package io.github.jevaengine.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.PolicyViolationException;
import io.github.jevaengine.communication.ShareEntityException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.netcommon.user.AuthenticationQuery;
import io.github.jevaengine.netcommon.user.NetUser;
import io.github.jevaengine.netcommon.user.Signal;
import io.github.jevaengine.netcommon.user.UserCredentials;
import io.github.jevaengine.netcommon.user.WorldShareRequestApplication;
import io.github.jevaengine.netcommon.user.WorldShareRequestApplicationDeclined;
import io.github.jevaengine.script.ScriptHiddenMember;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.server.ServerCertificate.ServerCertificateBridge;
import io.github.jevaengine.world.entity.DefaultEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

@SharedClass(name = "User", policy = SharePolicy.ClientR)
public final class ServerUser extends SharedEntity
{
	private static final int SYNC_INTERVAL = 200;
	
	private ServerCommunicator m_communicator;
	
	private boolean m_isAuthenticated = false;

	private String m_username;
		
	private IServerUserHandler m_handler;

	private int m_ping = 0;
	private int m_pingDispatch = 0;

	private IServerWorldLookup m_worldLookup;
	private ArrayList<ServerCertificate> m_ownedCertificates = new ArrayList<>();
	
	private ServerWorld m_presentlySharedWorld;
	private ServerUserScriptBridge m_scriptBridge = new ServerUserScriptBridge();
	
	public ServerUser(ServerCommunicator communicator, IServerUserHandler handler)
	{
		super(SYNC_INTERVAL);

		m_worldLookup = Core.getService(ServerGame.class).getServerWorldLookup();
		m_communicator = communicator;
		m_handler = handler;
	}

	public <T extends DefaultEntity> void grantCertificate(ServerCertificate certificate) throws PolicyViolationException, IOException, ShareEntityException
	{
		m_ownedCertificates.add(certificate);
		share(certificate);
	}
	
	public void clearCertificates()
	{
		Iterator<ServerCertificate> it = m_ownedCertificates.iterator();
		
		while(it.hasNext())
		{
			unshare(it.next());
			it.remove();
		}
	}
	
	public void revokeCertificate(ServerCertificate certificate) throws IOException
	{
		if(!m_ownedCertificates.contains(certificate))
			return;
		
		m_ownedCertificates.remove(certificate);
		unshare(certificate);
	}
	
	public boolean isCommunicator(Communicator communicator)
	{
		return m_communicator == communicator;
	}
	
	public ServerUserScriptBridge getScriptBridge()
	{
		return m_scriptBridge;
	}
	
	public boolean isAuthenticated()
	{
		return m_isAuthenticated;
	}

	public String getUsername()
	{
		return m_username;
	}
	
	void deauthorize()
	{
		if(isAuthenticated())
		{
			clearCertificates();
			send(Signal.AuthenticationInvalidate);
			m_handler.deauthenticated();
			m_scriptBridge.onDeauthorized.fire();
		}
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if (recv instanceof Signal)
		{
			if (((Signal) recv) == Signal.Ping)
				m_ping = 0;
		} else if (recv instanceof AuthenticationQuery)
		{
			AuthenticationQuery query = (AuthenticationQuery) recv;

			if (m_handler.login(query.getCredentials()))
			{
				m_isAuthenticated = true;
				m_username = query.getCredentials().getNickname();
				send(Signal.AuthenticationSucceeded);
				
				m_handler.authenticated();
				
				m_scriptBridge.onAuthorized.fire();
			} else
				send(Signal.AuthenticationFailed);
		}else if(recv instanceof WorldShareRequestApplication)
		{
			ServerWorld world = m_worldLookup.lookupServerWorld(((WorldShareRequestApplication)recv).getWorld());
			
			if(world != null)
			{
				if(!isSharing(world))
				{
					share(world);
					
					if(m_presentlySharedWorld != null)
						unshare(m_presentlySharedWorld);
						
					m_presentlySharedWorld = world;
				}
			}else
				send(new WorldShareRequestApplicationDeclined(((WorldShareRequestApplication)recv)));
		} else
			throw new InvalidMessageException(sender, recv, "Unrecognized message.");

		return true;
	}

	@Override
	public void doSynchronization() throws InvalidMessageException
	{
		for(ServerCertificate certificate : m_ownedCertificates)
			certificate.synchronize();
	}
	
	@Override
	public void doLogic(int deltaTime)
	{
		m_pingDispatch += deltaTime;
		m_ping += deltaTime;

		if (m_pingDispatch >= NetUser.PING_INTERVAL)
		{
			m_pingDispatch = 0;
			send(Signal.Ping);
		}

		if (m_ping >= NetUser.PING_TIMEOUT)
		{
			m_ping = 0;
			m_handler.pingTimeout();
		}

		for(ServerCertificate certificate : m_ownedCertificates)
			certificate.update(deltaTime);
	}

	public interface IServerUserHandler
	{
		boolean login(UserCredentials credentials);
		void pingTimeout();
		
		void authenticated();
		void deauthenticated();
	}

	public final class ServerUserScriptBridge
	{
		public ScriptEvent onAuthorized = new ScriptEvent();
		public ScriptEvent onDeauthorized = new ScriptEvent();
		
		@ScriptHiddenMember
		public ServerUser getServerUser()
		{
			return ServerUser.this;
		}
		
		public boolean isAuthenticated()
		{
			return ServerUser.this.isAuthenticated();
		}
		
		public String getUsername()
		{
			return ServerUser.this.getUsername();
		}
		
		public void grantCertificate(ServerCertificateBridge<?> certificate) throws PolicyViolationException, ShareEntityException, IOException
		{
			ServerUser.this.grantCertificate(certificate.getCertificate());
		}
		
		public void revokeCertificate(ServerCertificateBridge<?> certificate) throws IOException
		{
			ServerUser.this.revokeCertificate(certificate.getCertificate());
		}
	}
}
