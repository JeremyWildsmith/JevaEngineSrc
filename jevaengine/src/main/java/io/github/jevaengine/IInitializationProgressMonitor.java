package io.github.jevaengine;

public interface IInitializationProgressMonitor
{
	void statusChanged(float progress, String status);
}
