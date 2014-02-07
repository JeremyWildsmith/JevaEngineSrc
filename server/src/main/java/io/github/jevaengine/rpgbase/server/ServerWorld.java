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
package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.PolicyViolationException;
import io.github.jevaengine.communication.ShareEntityException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.rpgbase.netcommon.NetWorld;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.WorldConfiguration;

import java.io.IOException;
import java.util.ArrayList;

@SharedClass(name = "World", policy = SharePolicy.ClientR)
public final class ServerWorld extends NetWorld implements IDisposable
{
	private static final int SYNC_INTERVAL = 200;

	private ArrayList<ServerEntity<? extends Entity>> m_serverEntities = new ArrayList<ServerEntity<? extends Entity>>();

	private String m_worldName;

	private World m_world;

	private int m_tickCount = 0;

	public ServerWorld(String worldName)
	{
		m_worldName = worldName;

		m_world = World.create(Core.getService(ResourceLibrary.class).openConfiguration(worldName).getValue(WorldConfiguration.class));
	}

	@Override
	public void dispose()
	{
		m_world.dispose();
	}

	private void resyncEntity(ServerEntity<? extends Entity> entity)
	{
		try
		{
			entity.synchronize();
		} catch (InvalidMessageException e)
		{
			ServerCommunicator sender = (ServerCommunicator) e.getSender();

			if (sender.isConnected())
				sender.disconnect("Invalid message: " + e.toString());

			// Try again...
			resyncEntity(entity);
		}
	}

	public void update(int deltaTime)
	{
		m_tickCount += deltaTime;

		m_world.update(deltaTime);

		for (ServerEntity<? extends Entity> entity : m_serverEntities)
		{
			resyncEntity(entity);
			entity.update(deltaTime);
		}

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
	}

	public String getName()
	{
		return m_worldName;
	}

	protected World getWorld()
	{
		return m_world;
	}

	protected synchronized void entityEnter(ServerEntity<? extends Entity>  entity)
	{
		m_serverEntities.add(entity);

		try
		{
			share(entity);
		} catch (IOException | ShareEntityException | PolicyViolationException e)
		{
			throw new RuntimeException(e);
		}
	}

	protected synchronized void entityLeave(ServerEntity<? extends Entity>  entity)
	{
		m_serverEntities.remove(entity);

		try
		{
			unshare(entity);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv)
	{
		try
		{
			if (recv instanceof InitializeRequest)
			{
				send(sender, new InitializationArguments(m_worldName));
			} else if (recv instanceof IWorldVisitor)
			{
				IWorldVisitor visitor = (IWorldVisitor) recv;

				if (visitor.isServerDispatchOnly())
					throw new InvalidMessageException(sender, recv, "This visitor is server only.");

				visitor.visit(sender, m_world);
			} else
				throw new InvalidMessageException(sender, recv, "Unrecognize message recieved from server");

		} catch (InvalidMessageException e)
		{
			((ServerCommunicator) sender).disconnect(e.toString());
		}

		return true;
	}
}
