package io.github.jevaengine.world.entity;

import io.github.jevaengine.world.entity.tasks.ITask;

public interface IEntityTaskModel
{
	void addTask(ITask task);
	void cancelTasks();
	void cancelTask(ITask task);
	boolean isTaskActive(ITask task);
	boolean isTaskBlocking();
	void update(int deltaTime);
}
