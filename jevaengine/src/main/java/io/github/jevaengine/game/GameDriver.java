package io.github.jevaengine.game;

import io.github.jevaengine.IEngineThreadPool;
import io.github.jevaengine.IEngineThreadPool.Purpose;

import com.google.inject.Inject;

public final class GameDriver
{
	private final IGame m_game;	
	private final IEngineThreadPool m_threadPool;
	private final IRenderer m_renderer;
	
	private volatile boolean m_runGame = true;
	private volatile boolean m_isRunning = false;
	
	@Inject
	public GameDriver(IGameFactory gameFactory, IEngineThreadPool threadPool, IRenderer renderer)
	{
		m_game = gameFactory.create();
		m_threadPool = threadPool;
		m_renderer = renderer;
	}
	
	public void begin()
	{
		if(m_isRunning)
			return;
		
		m_runGame = true;
		m_isRunning = true;
		
		m_threadPool.execute(Purpose.GameLogic, new GameLogicDriver());
	}
	
	public void stop()
	{
		m_runGame = false;
	}
	
	private class GameLogicDriver implements Runnable
	{
		public void run()
		{
			long lastTime;
			
			do
			{
				lastTime = System.currentTimeMillis();
				
				try
				{
					Thread.sleep(10);
				} catch (InterruptedException e)
				{
					Thread.interrupted();
				}
				
				m_game.render(m_renderer);
				m_game.update((int)(System.currentTimeMillis() - lastTime));
				
			} while(m_runGame);
			
			m_isRunning = false;
		}
	}
}
