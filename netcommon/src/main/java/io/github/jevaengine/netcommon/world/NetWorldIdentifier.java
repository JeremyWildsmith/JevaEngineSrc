package io.github.jevaengine.netcommon.world;

public final class NetWorldIdentifier
{
	private String m_worldName;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private NetWorldIdentifier() { }
	
	public NetWorldIdentifier(String worldName)
	{
		m_worldName = worldName.trim().replace('\\', '/');
	}
	
	public String getFormalName()
	{
		return m_worldName;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o instanceof NetWorldIdentifier)
		{
			return m_worldName.equals(((NetWorldIdentifier)o).m_worldName);
		}else
			return false;
	}
	
	@Override
	public int hashCode()
	{
		return m_worldName.hashCode();
	}
}
