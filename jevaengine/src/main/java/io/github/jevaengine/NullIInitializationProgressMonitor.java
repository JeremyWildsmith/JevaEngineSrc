package io.github.jevaengine;

public final class NullIInitializationProgressMonitor implements IInitializationProgressMonitor
{

	@Override
	public void statusChanged(float progress, String status) { }

}
