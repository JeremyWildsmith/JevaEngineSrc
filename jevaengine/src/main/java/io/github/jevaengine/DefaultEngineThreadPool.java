package io.github.jevaengine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class DefaultEngineThreadPool implements IEngineThreadPool
{
	private static final int LOADING_THREADS = 5;
	
	private ExecutorService m_daemonExecutor = Executors.newCachedThreadPool(new ThreadFactory() {	
		@Override
		public Thread newThread(Runnable r)
		{
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setDaemon(true);
			thread.setPriority(Thread.MIN_PRIORITY);
			
			return thread;
		}
	});
	
	private ExecutorService m_loadingExecutor = Executors.newFixedThreadPool(LOADING_THREADS);
	
	private ExecutorService m_longLivingLowPriorityExecutor = Executors.newCachedThreadPool();
	
	private ExecutorService m_engineLogicExector = Executors.newSingleThreadExecutor();
	
	@Override
	public void execute(Purpose purpose, Runnable task)
	{
		switch(purpose)
		{
		case Loading:
			m_loadingExecutor.execute(task);
			break;
		case LongLivingLowPriority:
			m_longLivingLowPriorityExecutor.execute(task);
			break;
		case LongLivingLowPriorityDaemon:
			m_daemonExecutor.execute(task);
			break;
		case GameLogic:
			m_engineLogicExector.execute(task);
			break;
		default:
			throw new UnsupportedOperationException("Unrecognized task purpose.");
		}
	}

}
