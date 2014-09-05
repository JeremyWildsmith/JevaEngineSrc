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

import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.netcommon.user.AuthenticationQuery;
import io.github.jevaengine.netcommon.user.NetUser;
import io.github.jevaengine.netcommon.user.Signal;
import io.github.jevaengine.netcommon.user.UserCredentials;
import io.github.jevaengine.netcommon.user.WorldShareRequestApplication;
import io.github.jevaengine.netcommon.user.WorldShareRequestApplicationDeclined;
import io.github.jevaengine.netcommon.world.NetWorldIdentifier;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;

@SharedClass(name = "User", policy = SharePolicy.ClientR)
public final class ClientUser extends SharedEntity
{
	private static final int SYNC_INTERVAL = 200;

	private int m_ping = 0;

	private int m_pingDispatch = 0;

	private CertificatesObservers m_certificatesObservers = new CertificatesObservers();
	private TimeoutObservers m_timeoutObservers = new TimeoutObservers();
	
	private ArrayList<IClientCertificate> m_certificates = new ArrayList<>();
	private ArrayList<IClientCertificate> m_validatingCertificates = new ArrayList<>();
	
	private ArrayList<WorldShareRequestHandler> m_worldRequests = new ArrayList<>();
	private ArrayList<ClientWorld> m_worlds = new ArrayList<>();
	private ArrayList<ClientWorld> m_validatingWorlds = new ArrayList<>();
	
	private ArrayList<SharedEntity> m_exclusiveSharedEntities = new ArrayList<>();
	
	private boolean m_isAuthenticated = false;
	@Nullable
	private IClientUserAuthenticationHandler m_currentAuthenticationHandler;
	
	public ClientUser()
	{
		super(SYNC_INTERVAL);
	}

	public void addTimeoutObserver(IClientUserTimeoutObserver o)
	{
		m_timeoutObservers.add(o);
	}
	
	public void removeTimeoutObserver(IClientUserTimeoutObserver o)
	{
		m_timeoutObservers.remove(o);
	}

	public void addEntityRightsCertificationObserver(IClientUserCertificatesObserver o)
	{
		m_certificatesObservers.add(o);
	}
	
	public void removeEntityRightsCertificationObserver(IClientUserCertificatesObserver o)
	{
		m_certificatesObservers.remove(o);
	}
	
	public boolean isAuthenticated()
	{
		return m_isAuthenticated;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends IClientCertificate> T[] getCertificates(Class<T> certificateClass)
	{
		ArrayList<T> certificates = new ArrayList<>();
		
		for(IClientCertificate c : m_certificates)
		{
			if(certificateClass.isAssignableFrom(c.getClass()))
				certificates.add((T)c);
		}
		
		return certificates.toArray((T[])Array.newInstance(certificateClass, certificates.size()));
	}
	
	@Nullable
	private ClientWorld getSharedWorld(NetWorldIdentifier worldName)
	{
		for(ClientWorld world : m_worlds)
		{
			if(world.getWorldName().equals(worldName))
				return world;
		}
		
		return null;
	}
	
	private WorldShareRequestHandler getShareWorldRequestHandler(NetWorldIdentifier world)
	{
		WorldShareRequestHandler worldHandler = null;
		
		for(WorldShareRequestHandler h : m_worldRequests)
		{
			if(h.getWorldName().equals(world))
				worldHandler = h;
		}
		
		if(worldHandler == null)
		{
			worldHandler = new WorldShareRequestHandler(world);
			m_worldRequests.add(worldHandler);
			send(new WorldShareRequestApplication(world));
		}
		
		return worldHandler;
	}
	
	private void worldValidated(ClientWorld world)
	{
		Iterator<WorldShareRequestHandler> it = m_worldRequests.iterator();
		
		while(it.hasNext())
		{
			WorldShareRequestHandler handler = it.next();
			
			if(world.getWorldName().equals(handler.getWorldName()))
			{
				handler.onWorldValidated(world);
				it.remove();
			}
		}
	}
	
	private void worldShareDeclined(NetWorldIdentifier name)
	{
		Iterator<WorldShareRequestHandler> it = m_worldRequests.iterator();
		
		while(it.hasNext())
		{
			WorldShareRequestHandler handler = it.next();
			
			if(name.equals(handler.getWorldName()))
			{
				handler.onWorldShareDeclined();
				it.remove();
			}
		}
	}
	
	public void requestWorld(String worldName, IInitializationMonitor<ClientWorld> monitor)
	{
		NetWorldIdentifier worldIdentifier = new NetWorldIdentifier(worldName);
		
		ClientWorld sharedWorld = getSharedWorld(worldIdentifier);
		
		if(sharedWorld != null)
			sharedWorld.monitorInitialization(monitor);
		else
		{
			WorldShareRequestHandler handler = getShareWorldRequestHandler(worldIdentifier);
			handler.monitor(monitor);
		}
	}
	
	public void queryAuthentication(UserCredentials credentials, IClientUserAuthenticationHandler handler) throws AlreadyAuthenticatedException, BusyAuthenticatingException
	{
		if(m_currentAuthenticationHandler != null)
		{
			if(isAuthenticated())
				throw new AlreadyAuthenticatedException();
			else
				throw new BusyAuthenticatingException();
		}
		
		m_currentAuthenticationHandler = handler;
		
		send(new AuthenticationQuery(credentials));
	}
	
	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if (recv instanceof Signal)
		{
			switch ((Signal) recv)
			{
				case Ping:
					m_ping = 0;
					break;
				case AuthenticationSucceeded:
					if(m_currentAuthenticationHandler == null)
						throw new InvalidMessageException(sender, recv, "Unexpected authentication response.");
					
					m_isAuthenticated = true;
					m_currentAuthenticationHandler.authenticated();
					break;
				case AuthenticationFailed:
					if(m_currentAuthenticationHandler == null)
						throw new InvalidMessageException(sender, recv, "Unexpected authentication response.");
					
					m_isAuthenticated = false;
					m_currentAuthenticationHandler.authenticationFailed();
					m_currentAuthenticationHandler = null;
					break;
				case AuthenticationInvalidate:
					if(!m_isAuthenticated)
						throw new InvalidMessageException(sender, recv, "Server attempted to invalidated authentication that did not exist.");
					
					m_isAuthenticated = false;
					m_currentAuthenticationHandler.authenticationInvalidated();
					m_currentAuthenticationHandler = null;
				default:
					throw new InvalidMessageException(sender, recv, "Unrecognized primitive query from server");
			}
		} else if(recv instanceof WorldShareRequestApplicationDeclined)
		{
			worldShareDeclined(((WorldShareRequestApplicationDeclined)recv).getRequest().getWorld());
		} else
			throw new InvalidMessageException(sender, recv, "Unrecognized message recieved from server.");

		return true;
	}

	@Override
	protected void onEntityShared(SharedEntity e)
	{
		if(e instanceof IClientCertificate)
		{
			m_validatingCertificates.add((IClientCertificate)e);
		}else if(e instanceof ClientWorld)
		{
			m_validatingWorlds.add((ClientWorld)e);
		}
		
		m_exclusiveSharedEntities.add(e);
	}
	
	@Override
	protected void onEntityUnshared(SharedEntity e)
	{
		if(e instanceof IClientCertificate)
		{
			IClientCertificate certificate = (IClientCertificate)e;
			
			m_validatingCertificates.remove(certificate);
			
			if(m_certificates.remove(certificate))
				m_certificatesObservers.certificatesChanged();
		}
		
		m_exclusiveSharedEntities.remove(e);
	}
	
	private void checkValidatingCertificates()
	{
		Iterator<IClientCertificate> it = m_validatingCertificates.iterator();
	
		while(it.hasNext())
		{
			IClientCertificate certificate = it.next();
			
			if(certificate.isInitialized())
			{
				m_certificates.add(certificate);
				it.remove();
				
				m_certificatesObservers.certificatesChanged();
			}
		}
	}
	
	private void checkValidatingWorlds()
	{
		Iterator<ClientWorld> it = m_validatingWorlds.iterator();
		
		while(it.hasNext())
		{
			ClientWorld world = it.next();
			
			if(world.isValid())
			{
				it.remove();
				m_worlds.add(world);
				
				worldValidated(world);
			}
		}
	}
	
	@Override
	protected void doLogic(int deltaTime)
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
			m_timeoutObservers.timeout();
		}

		for(SharedEntity e : m_exclusiveSharedEntities)
			e.update(deltaTime);
		
		Iterator<WorldShareRequestHandler> it = m_worldRequests.iterator();
		while(it.hasNext())
		{
			WorldShareRequestHandler handler = it.next();
			
			if(handler.update(deltaTime))
				it.remove();
		}
		
		checkValidatingCertificates();
		checkValidatingWorlds();
	}
	
	@Override
	protected void doSynchronization() throws InvalidMessageException
	{
		for(SharedEntity e : m_exclusiveSharedEntities)
			e.synchronize();
	}
	
	private static class WorldShareRequestHandler
	{
		private static final int REQUEST_TIMEOUT = 100000;

		private NetWorldIdentifier m_worldName;
		private ArrayList<IInitializationMonitor<ClientWorld>> m_monitors = new ArrayList<>();
		private int m_timeSinceRequest;
		
		public WorldShareRequestHandler(NetWorldIdentifier worldName)
		{
			m_worldName = worldName;
		}
		
		public NetWorldIdentifier getWorldName()
		{
			return m_worldName;
		}
		
		public void onWorldValidated(ClientWorld world)
		{
			for(IInitializationMonitor<ClientWorld> m : m_monitors)
				world.monitorInitialization(m);
		}
		
		public void onWorldShareDeclined()
		{
			for(IInitializationMonitor<ClientWorld> m : m_monitors)
				m.completed(new FutureResult<ClientWorld>(new ShareWorldRequestDeclinedException()));
		}
		
		public void monitor(IInitializationMonitor<ClientWorld> monitor)
		{
			m_monitors.add(monitor);
		}
		
		public boolean update(int deltaTime)
		{
			m_timeSinceRequest += deltaTime;
			
			if(m_timeSinceRequest >= REQUEST_TIMEOUT)
			{
				for(IInitializationMonitor<ClientWorld> m : m_monitors)
					m.completed(new FutureResult<ClientWorld>(new ShareWorldRequestTimedOutException()));
				
				return true;
			}else
				return false;
		}
	}
	
	public static final class ShareWorldRequestDeclinedException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		private ShareWorldRequestDeclinedException() { }
	}
	
	public static final class ShareWorldRequestTimedOutException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		private ShareWorldRequestTimedOutException() { }
	}
	
	
	private static class TimeoutObservers extends StaticSet<IClientUserTimeoutObserver>
	{
		public void timeout()
		{
			for (IClientUserTimeoutObserver o : this)
				o.timeout();
		}
	}
	
	private static class CertificatesObservers extends StaticSet<IClientUserCertificatesObserver>
	{
		public void certificatesChanged()
		{
			for (IClientUserCertificatesObserver o : this)
				o.entityCertificationChanged();	
		}
	}

	public interface IClientUserAuthenticationHandler
	{
		void authenticated();
		void authenticationFailed();
		void authenticationInvalidated();
	}
	
	public interface IClientUserTimeoutObserver
	{
		void timeout();
	}
	
	public interface IClientUserCertificatesObserver
	{
		void entityCertificationChanged();
	}
	
	public final class AlreadyAuthenticatedException extends Exception
	{
		private static final long serialVersionUID = 1L;

		private AlreadyAuthenticatedException() {}
	}
	
	public final class BusyAuthenticatingException extends Exception
	{
		private static final long serialVersionUID = 1L;

		private BusyAuthenticatingException() { }		
	}
}
