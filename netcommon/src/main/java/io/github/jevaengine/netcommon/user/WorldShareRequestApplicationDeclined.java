package io.github.jevaengine.netcommon.user;


public class WorldShareRequestApplicationDeclined
{
	private WorldShareRequestApplication m_request;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private WorldShareRequestApplicationDeclined()
	{
	}
	
	public WorldShareRequestApplicationDeclined(WorldShareRequestApplication request)
	{
		m_request = request;
	}
	
	public WorldShareRequestApplication getRequest()
	{
		return m_request;
	}
}