package io.github.jevaengine.world;

import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IEngineThreadPool;
import io.github.jevaengine.IEngineThreadPool.Purpose;
import io.github.jevaengine.world.IWorldFactory.WorldConstructionException;
import io.github.jevaengine.IInitializationMonitor;

public final class ThreadPooledWorldFactory implements IParallelWorldFactory
{
	private final IWorldFactory m_worldFactory;
	private final IEngineThreadPool m_threadPool;
	
	public ThreadPooledWorldFactory(IWorldFactory worldFactory, IEngineThreadPool threadPool)
	{
		m_worldFactory = worldFactory;
		m_threadPool = threadPool;
	}
	
	@Override
	public void create(final String name, final float tileWidthMeters, final float tileHeightMeters, final IInitializationMonitor<World, WorldConstructionException> monitor)
	{
		m_threadPool.execute(Purpose.Loading, new Runnable() {
			@Override
			public void run()
			{
				try
				{
					monitor.completed(new FutureResult<World, WorldConstructionException>(m_worldFactory.create(name, tileWidthMeters, tileHeightMeters, monitor)));
				} catch(WorldConstructionException e)
				{
					monitor.completed(new FutureResult<World, WorldConstructionException>(e));
				}
			}
		});
	}
}
