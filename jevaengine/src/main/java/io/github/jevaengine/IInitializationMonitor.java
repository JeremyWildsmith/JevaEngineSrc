package io.github.jevaengine;

public interface IInitializationMonitor<T, Y extends Exception> extends IInitializationProgressMonitor
{
	void completed(FutureResult<T, Y> result);
}
