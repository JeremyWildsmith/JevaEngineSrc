package io.github.jevaengine.netcommon.world;


public final class InitializationArguments
{
	private NetWorldIdentifier m_worldName;

	@SuppressWarnings("unused")
	// Used by Kryo
	private InitializationArguments() { }

	public InitializationArguments(NetWorldIdentifier worldName)
	{
		m_worldName = worldName;
	}

	public NetWorldIdentifier getWorldName()
	{
		return m_worldName;
	}
}