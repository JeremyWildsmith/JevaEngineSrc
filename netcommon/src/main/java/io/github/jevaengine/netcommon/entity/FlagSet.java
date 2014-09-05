package io.github.jevaengine.netcommon.entity;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.world.entity.DefaultEntity.EntityController;

@SuppressWarnings("rawtypes")
public final class FlagSet implements INetVisitor<EntityController>
{
	public String m_name;
	public Integer m_value;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private FlagSet() { }
	
	public FlagSet(String name, Integer value)
	{
		m_name = name;
		m_value = value;
	}

	@Override
	public void visit(Communicator sender, EntityController entity, boolean onServer) throws InvalidMessageException
	{
		entity.setFlag(m_name, m_value);
	}

	@Override
	public Class<EntityController> getHostComponentClass()
	{
		return EntityController.class;
	}
}