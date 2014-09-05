package io.github.jevaengine.world.entity;

import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntityFactory.EntityConstructionException;

public interface IParallelEntityFactory
{
	<T extends IEntity> void create(final Class<T> entityClass, @Nullable final String instanceName, final IImmutableVariable config, final IInitializationMonitor<T, EntityConstructionException> monitor);
	void create(final String entityName, @Nullable final String instanceName, final IImmutableVariable config, final IInitializationMonitor<IEntity, EntityConstructionException> monitor);
	<T extends IEntity> void create(final Class<T> entityClass, @Nullable final String instanceName, final String config, final IInitializationMonitor<T, EntityConstructionException> monitor);
	void create(final String entityName, @Nullable final String instanceName, final String config, final IInitializationMonitor<IEntity, EntityConstructionException> monitor);
}
