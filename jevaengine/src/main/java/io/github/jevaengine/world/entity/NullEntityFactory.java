package io.github.jevaengine.world.entity;

import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.config.IImmutableVariable;

public final class NullEntityFactory implements IEntityFactory, IParallelEntityFactory
{
	@Override
	public <T extends IEntity> void create(Class<T> entityClass, String instanceName, IImmutableVariable config, IInitializationMonitor<T, EntityConstructionException> monitor)
	{
		monitor.completed(new FutureResult<T, EntityConstructionException>(new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities())));
	}

	@Override
	public void create(String entityName, String instanceName, IImmutableVariable config, IInitializationMonitor<IEntity, EntityConstructionException> monitor)
	{
		monitor.completed(new FutureResult<IEntity, EntityConstructionException>(new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities())));
	}

	@Override
	public <T extends IEntity> void create(Class<T> entityClass, String instanceName, String config, IInitializationMonitor<T, EntityConstructionException> monitor)
	{
		monitor.completed(new FutureResult<T, EntityConstructionException>(new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities())));
	}

	@Override
	public void create(String entityName, String instanceName, String config, IInitializationMonitor<IEntity, EntityConstructionException> monitor)
	{
		monitor.completed(new FutureResult<IEntity, EntityConstructionException>(new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities())));
	}

	@Override
	public Class<? extends DefaultEntity> lookup(String className)
	{
		return null;
	}

	@Override
	public <T extends IEntity> String lookup(Class<T> entityClass)
	{
		return null;
	}

	@Override
	public <T extends IEntity> T create(Class<T> entityClass, String instanceName, IImmutableVariable config) throws EntityConstructionException
	{
		throw new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities());
	}

	@Override
	public IEntity create(String entityName, String instanceName, IImmutableVariable config) throws EntityConstructionException
	{
		throw new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities());
	}

	@Override
	public <T extends IEntity> T create(Class<T> entityClass, String instanceName, String config) throws EntityConstructionException
	{
		throw new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities());
	}

	@Override
	public IEntity create(String entityName, String instanceName, String config) throws EntityConstructionException
	{
		throw new EntityConstructionException(instanceName, new NullEntityFactoryCannotConstructEntities());
	}
	
	public static final class NullEntityFactoryCannotConstructEntities extends Exception
	{
		private static final long serialVersionUID = 1L;
	
		private NullEntityFactoryCannotConstructEntities() { }
	}
}
