package io.github.jevaengine.world.entity;

import io.github.jevaengine.world.entity.tasks.ITask;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public final class DefaultEntityTaskModel implements IEntityTaskModel
{
	private ArrayList<ITask> m_pendingTasks = new ArrayList<ITask>();
	private ArrayList<ITask> m_runningTasks = new ArrayList<ITask>();

	private DefaultEntity m_host;
	
	public DefaultEntityTaskModel(DefaultEntity host)
	{
		m_host = host;
	}
	
	public void addTask(ITask task)
	{
		m_pendingTasks.add(task);
	}

	public void cancelTasks()
	{
		for (ITask task : m_runningTasks)
			task.cancel();

		m_pendingTasks.clear();
	}

	public void cancelTask(ITask task)
	{
		if (!m_runningTasks.contains(task))
			throw new NoSuchElementException();

		task.cancel();
	}

	public boolean isTaskActive(ITask task)
	{
		return m_runningTasks.contains(task);
	}

	public boolean isTaskBlocking()
	{
		for (ITask task : m_runningTasks)
		{
			if (!task.isParallel())
				return true;
		}

		return false;
	}
	
	public void update(int deltaTime)
	{
		ArrayList<ITask> garbageTasks = new ArrayList<ITask>();

		LinkedList<ITask> reassignList = new LinkedList<ITask>();

		boolean isBlocking = isTaskBlocking();
		
		for (ITask task : m_pendingTasks)
		{
			if (task.isParallel())
				reassignList.add(task);
			else if(!isBlocking)
			{
				isBlocking = true;
				reassignList.add(task);
			}
		}

		for (ITask task : reassignList)
		{
			task.begin(m_host);
			m_runningTasks.add(task);
			m_pendingTasks.remove(task);
		}

		for (ITask task : m_runningTasks)
		{
			if (task.doCycle(deltaTime))
				garbageTasks.add(task);
		}

		for (ITask task : garbageTasks)
		{
			task.end();
			m_runningTasks.remove(task);
		}
	}
}
