package io.github.jevaengine.netcommon.user;

import io.github.jevaengine.netcommon.world.NetWorldIdentifier;

public class WorldShareRequestApplication
{
	private NetWorldIdentifier m_worldName;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private WorldShareRequestApplication()
	{
	}
	
	public WorldShareRequestApplication(NetWorldIdentifier worldName)
	{
		m_worldName = worldName;
	}
	
	public NetWorldIdentifier getWorld()
	{
		return m_worldName;
	}
}