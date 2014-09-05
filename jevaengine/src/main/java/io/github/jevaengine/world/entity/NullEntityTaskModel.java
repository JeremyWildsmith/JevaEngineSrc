package io.github.jevaengine.world.entity;

import io.github.jevaengine.world.entity.tasks.ITask;

public final class NullEntityTaskModel implements IEntityTaskModel
{

	@Override
	public void addTask(ITask task) { }

	@Override
	public void cancelTasks() { }

	@Override
	public void cancelTask(ITask task) { }

	@Override
	public boolean isTaskActive(ITask task)
	{
		return false;
	}

	@Override
	public boolean isTaskBlocking()
	{
		return false;
	}

	@Override
	public void update(int deltaTime) { }
}
