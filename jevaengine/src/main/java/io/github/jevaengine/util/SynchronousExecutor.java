package io.github.jevaengine.util;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SynchronousExecutor
{
	private Queue<ISynchronousTask> m_tasks = new ConcurrentLinkedQueue<>();
	
	public void enqueue(ISynchronousTask task)
	{
		if(task == null)
			throw new NullPointerException();
		
		m_tasks.add(task);
	}
	
	public void execute()
	{
		ArrayList<ISynchronousTask> requeue = new ArrayList<>();
		
		for(ISynchronousTask r; (r = m_tasks.poll()) != null;)
		{
			if(!r.run())
				requeue.add(r);
		}
		
		m_tasks.addAll(requeue);
	}
	
	public interface ISynchronousTask
	{
		boolean run();
	}
}
