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
import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.tcp.RemoteSocketCommunicator;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.game.DefaultGame;
import io.github.jevaengine.game.IGameScriptProvider;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration;
import io.github.jevaengine.netcommon.world.NetWorldIdentifier;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.server.ServerClientPool.IServerClientPoolObserver;
import io.github.jevaengine.server.ServerWorld.ServerWorldBridge;
import io.github.jevaengine.util.SynchronousExecutor;
import io.github.jevaengine.util.SynchronousExecutor.ISynchronousTask;
import io.github.jevaengine.world.World;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ServerGame extends DefaultGame implements IDisposable
{
	private final Logger m_logger = LoggerFactory.getLogger(ServerGame.class);

	private ServerListener m_listener;
	
	private ServerWorldPool m_worldPool = new ServerWorldPool();
	private ServerClientPool m_clientPool = new ServerClientPool();
	
	private Sprite m_cursor;

	private SynchronousExecutor m_syncExecuter = new SynchronousExecutor();
	
	public void startup()
	{
		ResourceLibrary library = Core.getService(ResourceLibrary.class);

		ServerConfiguration config = library.openConfiguration("server.cfg").getValue(ServerConfiguration.class);

		m_cursor = Sprite.create(library.openConfiguration("ui/tech/cursor/cursor.jsf").getValue(SpriteDeclaration.class));
		m_cursor.setAnimation("idle", AnimationState.Play);

		ServerScriptHandler serverScriptHandler = new ServerScriptHandler(config.script);
		m_clientPool.addObserver(serverScriptHandler);
		
		try
		{
			ServerSocket serverSocket = new ServerSocket(config.port);
			m_listener = new ServerListener(serverSocket);
		} catch (IOException e)
		{
			m_logger.error("Unable to initialize server: " + e.toString());
		}
	}

	@Override
	public void dispose()
	{
		m_listener.stop();
	}

	@Override
	public abstract ServerGameScriptProvider getScriptProvider();

	@Override
	protected Sprite getCursor()
	{
		return m_cursor;
	}

	@Override
	public boolean update(int deltaTime)
	{
		m_clientPool.update(deltaTime);
		
		m_worldPool.update(deltaTime, m_clientPool);
		
		m_syncExecuter.execute();
		
		return true;
	}
	
	public final IVisitAuthorizationPool getVisitAuthorizationPool()
	{
		return m_clientPool;
	}
	
	public final IServerWorldLookup getServerWorldLookup()
	{
		return m_worldPool;
	}
	
	public abstract class ServerGameScriptProvider implements IGameScriptProvider
	{
		public abstract ServerGameBridge getBridge();

		public abstract class ServerGameBridge
		{
			public final void getWorld(final String worldName, final Function loadSuccessCallback)
			{
				m_worldPool.getWorld(new NetWorldIdentifier(worldName), new IInitializationMonitor<ServerWorld>() {
					@Override
					public void statusChanged(float progress, String status) { }
					
					@Override
					public void completed(final FutureResult<ServerWorld> item)
					{
						m_syncExecuter.enqueue(new ISynchronousTask() {		
							@Override
							public boolean run()
							{
								ContextFactory.getGlobal().call(new ContextAction() {
									@Override
									public Object run(Context cx) {
										try
										{
											return loadSuccessCallback.call(cx, loadSuccessCallback.getParentScope(), null, new Object[] {item.get().getWorld().getScriptBridge()});
										}catch(Exception e)
										{
											throw new RuntimeException(e);
										}
									}
								});
								
								return true;
							}
						});
					}
				});
			}
			
			public final World.WorldBridge getWorld(String name)
			{
				ServerWorld world = m_worldPool.lookupServerWorld(new NetWorldIdentifier(name));

				return world == null ? null : world.getWorld().getScriptBridge();
			}
			
			public final ServerWorldBridge lookupNetworkComponent(World.WorldBridge worldBridge)
			{
				ServerWorld world = m_worldPool.lookupServerWorld(worldBridge.getWorld());
				
				return world == null ? null : world.getBridge();
			}
		}

		@Override
		public Map<String, Object> getGlobals()
		{
			return new HashMap<>();
		}
	}

	public static final class ServerScriptHandler implements IServerClientPoolObserver
	{	
		private ServerBridge m_bridge;
		
		public ServerScriptHandler(String script)
		{
			m_bridge = new ServerBridge();
			//If script does not register any ScriptableEvents to the ServerBridge, it will
			//become unreferenced and GCd, otherwise it will remain in memory as the ScriptableEvents will
			//reference the script.
			Core.getService(ResourceLibrary.class).openScript(script, m_bridge);
		}
		
		@Override
		public void userAuthenticated(ServerUser user)
		{
			m_bridge.onUserAuthenticated.fire(user.getScriptBridge());
		}

		@Override
		public void userDeauthenticated(ServerUser user)
		{
			m_bridge.onUserDeauthenticated.fire(user.getScriptBridge());
		}
		
		public class ServerBridge
		{
			public ScriptEvent onUserAuthenticated = new ScriptEvent();
			public ScriptEvent onUserDeauthenticated = new ScriptEvent();
		}
	}
	
	private class ServerListener
	{
		private ServerSocket m_socket;

		private volatile boolean m_queryStop = false;

		private Listener m_listener;

		public ServerListener(ServerSocket socket)
		{
			m_socket = socket;

			m_listener = new Listener();

			m_logger.info("Server accepting connections");

			new Thread(m_listener).start();
		}

		public void stop()
		{
			m_queryStop = true;

			try
			{
				m_socket.close();
			} catch (IOException e)
			{}
		}

		private class Listener implements Runnable
		{
			@Override
			public void run()
			{
				while(!m_queryStop)
				{
					try
					{
						Socket remoteSocket = m_socket.accept();
						remoteSocket.setTcpNoDelay(true);
						m_clientPool.acceptClient(new RemoteSocketCommunicator(remoteSocket));
					} catch (IOException e)
					{
						if (!m_queryStop)
							m_logger.warn("Server listener ended prematurely");

						m_queryStop = true;
					}
				}
			}
		}
	}

	public static class ServerConfiguration implements ISerializable
	{
		public String script;
		public int port;

		public ServerConfiguration()
		{}

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("script").setValue(this.script);
			target.addChild("port").setValue(this.port);
		}

		@Override
		public void deserialize(IImmutableVariable source)
		{
			script = source.getChild("script").getValue(String.class);
			port = source.getChild("port").getValue(Integer.class);
		}
	}
}
