package io.github.jevaengine.netcommon.entity;

import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.config.json.JsonVariable;
import io.github.jevaengine.world.entity.DefaultEntity;
import io.github.jevaengine.world.entity.IEntityFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class InitializeEntity
{	
	private String m_className;
	private String m_configuration;
	private NetEntityIdentifier m_name;

	@SuppressWarnings("unused")
	// Used by Kryo
	private InitializeEntity() { }
	
	public InitializeEntity(String className, IImmutableVariable configuration, String name) throws ValueSerializationException
	{
		JsonVariable jsonBuffer = new JsonVariable();
		configuration.serialize(jsonBuffer);
		
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			jsonBuffer.serialize(bos, false);
			m_configuration = bos.toString("UTF8");
		} catch(IOException e)
		{
			//Shouldn't occur unless there is an error in the way any of these objects are behaving, thus
			//it is more approriately thrown as a runtime exception...
			throw new RuntimeException(e);
		}
		
		m_className = className;
		m_name = new NetEntityIdentifier(name);
	}
	
	public <T extends DefaultEntity> void create(Communicator sender, IEntityFactory entityFactory, Class<T> entityClass, boolean onServer, IInitializationMonitor<T> handler) throws InvalidMessageException
	{	
		throw new RuntimeException();
		/*
		Class<? extends Entity> entity = entityFactory.lookup(m_className);
		
		if(!entity.equals(entityClass))
			throw new InvalidMessageException(sender, this, "Specified class name does not translate to expected entity.");
		
		try
		{
			entityFactory.create(entityClass, m_name.get(onServer), JsonVariable.create(new ByteArrayInputStream(m_configuration.getBytes("UTF8"))), handler);
		} catch (IOException | VariableNotAnObjectException e)
		{
			throw new InvalidMessageException(sender, this, "Error occured creating net entity: " + e.toString());
		}*/
	}
}