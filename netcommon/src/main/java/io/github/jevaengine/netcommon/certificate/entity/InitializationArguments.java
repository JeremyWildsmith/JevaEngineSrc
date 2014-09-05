package io.github.jevaengine.netcommon.certificate.entity;

import io.github.jevaengine.netcommon.entity.NetEntityIdentifier;

public final class InitializationArguments
{
	private NetEntityIdentifier m_entityIdentifier;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private InitializationArguments() { }
	
	public InitializationArguments(NetEntityIdentifier entityIdentifier)
	{
		m_entityIdentifier = entityIdentifier;
	}
	
	public NetEntityIdentifier getEntityName()
	{
		return m_entityIdentifier;
	}
}