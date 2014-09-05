package io.github.jevaengine.netcommon.certificate.entity;

import io.github.jevaengine.netcommon.world.NetWorldIdentifier;
import io.github.jevaengine.util.Nullable;

public final class HostWorldAssignment
{
	@Nullable
	private NetWorldIdentifier m_hostWorld;
	
	public HostWorldAssignment() { }
	
	public HostWorldAssignment(@Nullable NetWorldIdentifier hostWorld)
	{
		m_hostWorld = hostWorld;
	}
	
	@Nullable
	public NetWorldIdentifier getHostWorld()
	{
		return m_hostWorld;
	}
}