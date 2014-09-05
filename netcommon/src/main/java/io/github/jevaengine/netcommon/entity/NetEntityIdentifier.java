package io.github.jevaengine.netcommon.entity;

public class NetEntityIdentifier
{
	private static final String SERVER_NAME_PREFIX = "__@SERVER";
	
	private String m_name;
	
	//Used by Kryo
	@SuppressWarnings("unused")
	private NetEntityIdentifier() { }
	
	public NetEntityIdentifier(String name)
	{
		m_name = name;
	}
	
	public String get(boolean isServerSide)
	{
		if(isServerSide)
		{
			//If it was dispatched from client, it will be encoded...
			if(m_name.startsWith(SERVER_NAME_PREFIX))
				return m_name.substring(SERVER_NAME_PREFIX.length());
			else
				return m_name;
		}
		else
			return SERVER_NAME_PREFIX + m_name;
	}
	
	public boolean isValid()
	{
		return m_name != null;
	}
}
