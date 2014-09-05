package io.github.jevaengine.world.entity;

import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IEngineThreadPool;
import io.github.jevaengine.IEngineThreadPool.Purpose;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntityFactory.EntityConstructionException;
import io.github.jevaengine.IInitializationMonitor;

public final class ThreadPooledEntityFactory implements IParallelEntityFactory
{
	private final IEntityFactory m_entityFactory;
	private final IEngineThreadPool m_threadPool;
	
	public ThreadPooledEntityFactory(IEntityFactory entityFactory, IEngineThreadPool threadPool)
	{
		m_entityFactory = entityFactory;
		m_threadPool = threadPool;
	}
	
	@Override
	public <T extends IEntity> void create(final Class<T> entityClass, @Nullable final String instanceName, final IImmutableVariable config, final IInitializationMonitor<T, EntityConstructionException> initializationMonitor)
	{
		m_threadPool.execute(Purpose.Loading, new Runnable() {
			@Override
			public void run()
			{
				try
				{
					initializationMonitor.completed(new FutureResult<T, EntityConstructionException>(m_entityFactory.create(entityClass, instanceName, config)));
				} catch(EntityConstructionException e)
				{
					initializationMonitor.completed(new FutureResult<T, EntityConstructionException>(e));
				}
			}
		});
	}

	@Override
	public void create(final String entityName, @Nullable final String instanceName, final IImmutableVariable config, final IInitializationMonitor<IEntity, EntityConstructionException> monitor)
	{
		m_threadPool.execute(Purpose.Loading, new Runnable() {
			@Override
			public void run()
			{
				try
				{
					monitor.completed(new FutureResult<IEntity, EntityConstructionException>(m_entityFactory.create(entityName, instanceName, config)));
				} catch(EntityConstructionException e)
				{
					monitor.completed(new FutureResult<IEntity, EntityConstructionException>(e));
				}
			}
		});
	}

	@Override
	public <T extends IEntity> void create(final Class<T> entityClass, @Nullable final String instanceName, final String config, final IInitializationMonitor<T, EntityConstructionException> monitor)
	{
		m_threadPool.execute(Purpose.Loading, new Runnable() {
			@Override
			public void run()
			{
				try
				{
					monitor.completed(new FutureResult<T, EntityConstructionException>(m_entityFactory.create(entityClass, instanceName, config)));
				} catch(EntityConstructionException e)
				{
					monitor.completed(new FutureResult<T, EntityConstructionException>(e));
				}
			}
		});
	}

	@Override
	public void create(final String entityName, @Nullable final String instanceName, final String config, final IInitializationMonitor<IEntity, EntityConstructionException> monitor)
	{
		m_threadPool.execute(Purpose.Loading, new Runnable() {
			@Override
			public void run()
			{
				try
				{
					monitor.completed(new FutureResult<IEntity, EntityConstructionException>(m_entityFactory.create(entityName, instanceName, config)));
				} catch(EntityConstructionException e)
				{
					monitor.completed(new FutureResult<IEntity, EntityConstructionException>(e));
				}
			}
		});
	}
}
