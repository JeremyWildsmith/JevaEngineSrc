package io.github.jevaengine.netcommon.entity;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.world.entity.DefaultEntity.EntityController;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("rawtypes")
public final class InitializeFlags implements INetVisitor<EntityController>
{
	HashMap<String, Integer> m_flags;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private InitializeFlags() { }
	
	public InitializeFlags(Map<String, Integer> flags)
	{
		m_flags = new HashMap<String, Integer>(flags);
	}
	
	@Override
	public void visit(Communicator sender, EntityController entity, boolean onServer) throws InvalidMessageException
	{
		entity.clearFlags();
		
		for(Map.Entry<String, Integer> f : m_flags.entrySet())
			entity.setFlag(f.getKey(), f.getValue());
	}

	@Override
	public Class<EntityController> getHostComponentClass()
	{
		return EntityController.class;
	}
}