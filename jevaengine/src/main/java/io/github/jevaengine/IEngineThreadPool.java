package io.github.jevaengine;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultEngineThreadPool.class)
public interface IEngineThreadPool
{
	public void execute(Purpose purpose, Runnable task);
	
	public enum Purpose
	{
		Loading,
		GameLogic,
		LongLivingLowPriority,
		LongLivingLowPriorityDaemon,
	}
}
