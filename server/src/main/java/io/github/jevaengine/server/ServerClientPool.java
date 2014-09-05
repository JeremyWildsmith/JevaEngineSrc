package io.github.jevaengine.server;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.PolicyViolationException;
import io.github.jevaengine.communication.ShareEntityException;
import io.github.jevaengine.communication.SnapshotSynchronizationException;
import io.github.jevaengine.communication.tcp.RemoteSocketCommunicator;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.netcommon.user.UserCredentials;
import io.github.jevaengine.server.ServerUser.IServerUserHandler;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerClientPool implements IInvalidMessageHandler, IVisitAuthorizationPool
{
	private final Logger m_logger = LoggerFactory.getLogger(ServerGame.class);

	private static final int USERNAME_MAX_LENGTH = 16;
	private static final int USERNAME_MIN_LENGTH = 5;

	private static final String USERNAME_MATCH_REGEX = "[a-zA-Z0-9]*";
	
	private HashMap<RemoteSocketCommunicator, RemoteClientManager> m_remoteCommunicatorToClient =  new HashMap<>();
	private HashMap<Communicator, RemoteClientManager> m_localCommunicatorToClient = new HashMap<>();
	
	private Queue<RemoteClientManager> m_timedOutClients = new LinkedList<>();
	
	private Observers m_observers = new Observers();
	
	public void addObserver(IServerClientPoolObserver observer)
	{
		m_observers.add(observer);
	}
	
	public void removeObserver(IServerClientPoolObserver observer)
	{
		m_observers.remove(observer);
	}
	
	@Override
	public void handle(InvalidMessageException e)
	{
		RemoteClientManager remote = getClientManager(e.getSender());
		
		if(remote == null)
			throw new NoSuchElementException();
		
		closeClient(remote, "Invalid message recieved from client: " + e.toString());
	}
	
	public void acceptClient(RemoteSocketCommunicator remoteCommunicator)
	{
		synchronized (m_remoteCommunicatorToClient)
		{
			try
			{
				RemoteClientManager clientManager = new RemoteClientManager(remoteCommunicator);

				m_remoteCommunicatorToClient.put(remoteCommunicator, clientManager);
			} catch (IOException | ShareEntityException | PolicyViolationException e)
			{
				closeRawConnection(remoteCommunicator, "Internal Server Error: " + e.toString());
			}
		}
	}

	private void closeRawConnection(RemoteSocketCommunicator communicator, String reason)
	{
		communicator.dispose();
		m_logger.info("Closed raw socket: " + reason);
	}
	
	private void closeClient(RemoteClientManager clientManager, @Nullable String reason)
	{
		synchronized (m_remoteCommunicatorToClient)
		{
			m_remoteCommunicatorToClient.remove(clientManager.getRemoteCommunicator());
			clientManager.dispose();
			m_logger.info("Closed remote client connection: " + reason == null ? "No exceptional reason" : reason);
		}
	}
	
	@Nullable
	private RemoteClientManager getClientManager(Communicator localCommunicator)
	{
		for(RemoteClientManager m : m_remoteCommunicatorToClient.values())
		{
			if(m.m_localCommunicator == localCommunicator)
				return m;
		}
		
		return null;
	}
	
	private void clientPingTimeout(RemoteClientManager clientManager)
	{
		m_timedOutClients.add(clientManager);
	}
	
	private boolean isUsernameAllocatable(String username)
	{
		synchronized (m_remoteCommunicatorToClient)
		{
			for(RemoteClientManager client : m_remoteCommunicatorToClient.values())
			{
				if (client.m_user.isAuthenticated())
				{
					String clientUsername = client.m_user.getUsername();

					if (clientUsername.toLowerCase().compareTo(username.toLowerCase()) == 0)
						return false;
				}
			}
			
			return username.length() <= USERNAME_MAX_LENGTH && username.length() >= USERNAME_MIN_LENGTH && username.matches(USERNAME_MATCH_REGEX);
		}
	}

	@Override
	public <T extends INetVisitor<Y>, Y> boolean isVisitAuthorized(Communicator sender, T visitor, Y host)
	{
		RemoteClientManager clientManager = m_localCommunicatorToClient.get(sender);
		
		if(clientManager == null)
			return false;
		
		return clientManager.isVisitorAuthorized(visitor, host);
	}
	
	@Override
	public <T extends INetVisitor<Y>, Y> void addVisitAuthorizer(Communicator recipient, Class<Y> hostClass, IVisitAuthorizer<T, Y> authorizer)
	{
		RemoteClientManager clientManager = m_localCommunicatorToClient.get(recipient);
		
		if(clientManager == null)
			return;
		
		clientManager.addVisitorAuthorizer(hostClass, authorizer);
	}
	
	@Override
	public <T extends INetVisitor<Y>, Y> void removeVisitAuthorizer(Communicator recipient, Class<Y> hostClass, IVisitAuthorizer<T, Y> authorizer)
	{
		RemoteClientManager clientManager = m_localCommunicatorToClient.get(recipient);
		
		if(clientManager == null)
			return;
		
		clientManager.removeVisitorAuthorizer(hostClass, authorizer);
	}
	
	public void update(int deltaTime)
	{
		synchronized(m_remoteCommunicatorToClient)
		{
			for(RemoteClientManager client; (client = m_timedOutClients.poll()) != null;)
				closeClient(client, "Timed out");
			
			for(RemoteClientManager client : m_remoteCommunicatorToClient.values())
			{
				try
				{
					client.update(deltaTime);
				} catch (InvalidMessageException | IOException | SnapshotSynchronizationException e)
				{
					closeClient(client, "Error: " + e.toString());
				}
			}
		}
	}
	
	public final class RemoteClientManager
	{
		private ServerCommunicator m_localCommunicator;
		private RemoteSocketCommunicator m_remoteCommunicator;
		private ServerUser m_user;

		private Map<Class<?>, HashSet<IVisitAuthorizer<?, ?>>> m_authorizers = new HashMap<>();
		
		private RemoteClientManager(RemoteSocketCommunicator remoteCommunicator) throws IOException, ShareEntityException, PolicyViolationException
		{
			m_remoteCommunicator = remoteCommunicator;
			m_localCommunicator = new ServerCommunicator();
			m_localCommunicator.bind(remoteCommunicator);

			m_user = new ServerUser(m_localCommunicator, new ServerUserHandler());
			
			m_localCommunicator.shareEntity(m_user);
			
			m_localCommunicatorToClient.put(m_localCommunicator, this);
		}
		
		private void dispose()
		{
			m_localCommunicatorToClient.remove(m_localCommunicator);
			m_user.deauthorize();
			m_localCommunicator.unbind();
			m_remoteCommunicator.dispose();
		}
		
		@SuppressWarnings("unchecked")
		public <T extends INetVisitor<Y>, Y> boolean isVisitorAuthorized(T visitor, Y host)
		{
			HashSet<IVisitAuthorizer<?, ?>> hostAuthorizers = m_authorizers.get(host);
			
			if(hostAuthorizers == null)
				return false;
			
			for(IVisitAuthorizer<?, ?> v : hostAuthorizers)
			{
				if(((IVisitAuthorizer<T, Y>)v).isVisitorAuthorized(visitor, host))
					return true;
			}
			
			return false;
		}
		
		public <T extends INetVisitor<Y>, Y> void removeVisitorAuthorizer(Class<Y> hostClass, IVisitAuthorizer<T, ?> authorizer)
		{
			HashSet<IVisitAuthorizer<?, ?>> hostAuthorizers = m_authorizers.get(hostClass);
			
			if(hostAuthorizers != null)
			{
				hostAuthorizers.remove(authorizer);
				
				if(hostAuthorizers.isEmpty())
					m_authorizers.remove(hostClass);
			}
		}

		public <T extends INetVisitor<Y>, Y> void addVisitorAuthorizer(Class<Y> hostClass, IVisitAuthorizer<T, Y> authorizer)
		{
			if(!m_authorizers.containsKey(hostClass))
				m_authorizers.put(hostClass, new HashSet<IVisitAuthorizer<?, ?>>());
		
			m_authorizers.get(hostClass).add(authorizer);
		}

		private RemoteSocketCommunicator getRemoteCommunicator()
		{
			return m_remoteCommunicator;
		}

		private void update(int deltaTime) throws InvalidMessageException, IOException, SnapshotSynchronizationException
		{
			m_user.synchronize();
			m_localCommunicator.update(deltaTime);
			m_user.update(deltaTime);
		}

		public ServerUser getUser()
		{
			return m_user;
		}

		private final class ServerUserHandler implements IServerUserHandler
		{
			@Override
			public boolean login(UserCredentials credentials)
			{
				return isUsernameAllocatable(credentials.getNickname());
			}
			
			@Override
			public void authenticated()
			{
				m_observers.userAuthenticated(m_user);
			}
	
			@Override
			public void deauthenticated()
			{
				m_observers.userDeauthenticated(m_user);
			}
			
			@Override
			public void pingTimeout()
			{
				clientPingTimeout(RemoteClientManager.this);
			}
		}
	}
	
	public interface IServerClientPoolObserver
	{
		void userAuthenticated(ServerUser user);
		void userDeauthenticated(ServerUser user);
	}
	
	private static final class Observers extends StaticSet<IServerClientPoolObserver>
	{
		void userAuthenticated(ServerUser user)
		{
			for(IServerClientPoolObserver o : this)
				o.userAuthenticated(user);
		}
		
		void userDeauthenticated(ServerUser user)
		{
			for(IServerClientPoolObserver o : this)
				o.userDeauthenticated(user);
		}
	}
}
