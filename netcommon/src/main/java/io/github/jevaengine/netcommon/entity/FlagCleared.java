package io.github.jevaengine.netcommon.entity;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.world.entity.DefaultEntity.EntityController;

@SuppressWarnings("rawtypes")
public final class FlagCleared implements INetVisitor<EntityController>
{
	public String m_name;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private FlagCleared() { }
	
	public FlagCleared(String name)
	{
		m_name = name;
	}

	@Override
	public void visit(Communicator sender, EntityController entity, boolean onServer) throws InvalidMessageException
	{
		entity.clearFlag(m_name);
	}

	@Override
	public Class<EntityController> getHostComponentClass()
	{
		return EntityController.class;
	}
}