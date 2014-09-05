package io.github.jevaengine.client;

import io.github.jevaengine.FutureResult;
import io.github.jevaengine.IInitializationMonitor;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.netcommon.entity.InitializeEntity;
import io.github.jevaengine.netcommon.entity.Signal;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.SynchronousExecutor.ISynchronousTask;
import io.github.jevaengine.world.entity.DefaultEntity;

import java.util.ArrayList;

public abstract class ClientEntity<T extends DefaultEntity> extends SharedEntity
{
	private static final int SYNC_INTERVAL = 50;

	private Class<T> m_class;
	
	@Nullable
	private volatile T m_entity;
	
	private ArrayList<IInitializationMonitor<T>> m_monitors = new ArrayList<>();
	
	public ClientEntity(Class<T> clazz)
	{
		super(SYNC_INTERVAL);
		m_class = clazz;

		enqueueLogicTask(new ISynchronousTask() {
			@Override
			public boolean run()
			{
				send(Signal.InitializeRequest);
				return true;
			}
		});
	}
	
	public void monitorInitialization(IInitializationMonitor<T> monitor)
	{
		synchronized(m_monitors)
		{
			if(m_entity != null)
				monitor.completed(new FutureResult<T>(m_entity));
			else
				m_monitors.add(monitor);
		}
	}
	
	public final boolean isReady()
	{
		return m_entity != null;
	}
	
	@Nullable
	public final T getEntity()
	{
		return m_entity;
	}

	@Override
	protected final boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if(recv instanceof INetVisitor<?>)
		{
			INetVisitor<?> genericVisitor = (INetVisitor<?>)recv;
			
			if(!genericVisitor.getHostComponentClass().isAssignableFrom(getEntity().getClass()))
				throw new InvalidMessageException(sender, recv, "Visitor cannot visit this component due to incompatable host component types");
			
			@SuppressWarnings("unchecked")
			INetVisitor<T> entityVisitor = (INetVisitor<T>)genericVisitor;
			
			entityVisitor.visit(sender, getEntity(), true);
		}else if(recv instanceof InitializeEntity)
		{
			InitializeEntity init = (InitializeEntity)recv;
			
			if(m_entity != null)
				throw new InvalidMessageException(sender, recv, "Attempted to initialize entity when it was already initialized.");
			
			init.create(sender, m_class, false, new IInitializationMonitor<T>() {
					@Override
					public void completed(final FutureResult<T> item)
					{
						try
						{
							m_entity = (T)item.get();
						
							synchronized(m_monitors)
							{
								for(IInitializationMonitor<T> m : m_monitors)
									m.completed(item);
								
								m_monitors.clear();
							}
						
						}catch(Exception e)
						{
							throw new RuntimeException(e);
						}
					}

					@Override
					public void statusChanged(float progress, String status)
					{
						synchronized(m_monitors)
						{
							for(IInitializationMonitor<T> m : m_monitors)
								m.statusChanged(progress, status);		
						}
					}
				});
		}else
			throw new InvalidMessageException(sender, recv, "Unrecognized message");

		return true;
	}
	
	public abstract void beginVisit();
	public abstract void endVisit();
}