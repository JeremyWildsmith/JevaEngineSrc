/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.communication;

import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.util.SynchronousExecutor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import io.github.jevaengine.util.SynchronousExecutor.ISynchronousTask;

public abstract class SharedEntity
{
	private int m_snapshotInterval;
	private int m_snapshotTickCount = 0;

	private ArrayList<SharedField<?>> m_sharedFields;
	private Queue<FieldSynchronizationQuery> m_fieldSyncQueue = new ConcurrentLinkedQueue<>();
	private Queue<MessageSynchronizationQuery> m_messageQueue = new ConcurrentLinkedQueue<>();
	private StaticSet<Communicator> m_boundCommunicators = new StaticSet<>();

	private SharedEntity m_parent;
	private ArrayList<SharedEntity> m_children = new ArrayList<SharedEntity>();
	private SynchronousExecutor m_syncLogicExecutor = new SynchronousExecutor();
	
	public SharedEntity(int snapshotInterval)
	{
		m_snapshotInterval = snapshotInterval;
	}

	void bindCommunicator(final Communicator listener) throws ShareEntityException
	{
		synchronized (m_children)
		{
			m_boundCommunicators.add(listener);

			for (SharedEntity e : m_children)
				listener.shareEntity(e);
		}
		
		m_syncLogicExecutor.enqueue(new ISynchronousTask()
		{
			@Override
			public boolean run()
			{
				onCommunicatorBound(listener);
				return true;
			}
		});
	}

	void unbindCommunicator(final Communicator listener)
	{
		synchronized (m_children)
		{
			m_boundCommunicators.remove(listener);

			for (SharedEntity e : m_children)
				listener.unshareEntity(e);
		}
		
		m_syncLogicExecutor.enqueue(new ISynchronousTask()
		{
			@Override
			public boolean run()
			{
				onCommunicatorUnbound(listener);
				return true;
			}
		});
	}
	
	void childEntityShared(final SharedEntity e)
	{
		m_syncLogicExecutor.enqueue(new ISynchronousTask()
		{
			@Override
			public boolean run()
			{
				//As to remain consistent with onEntityShared, child should be added synchronously.
				synchronized(m_children)
				{
					e.m_parent = SharedEntity.this;
					m_children.add(e);
					onEntityShared(e);
				}
				
				return true;
			}
		});
	}

	void childEntityUnshared(final SharedEntity e)
	{
		m_syncLogicExecutor.enqueue(new ISynchronousTask()
		{
			@Override
			public boolean run()
			{
				//As to remain consistent with onEntityUnshared, child should be removed.
				synchronized(m_children)
				{
					e.m_parent = null;
					m_children.remove(e);
					onEntityUnshared(e);
				}
				
				return true;
			}
		});
	}
	
	//Synchronized to prevent multiple calls to this method being executed concurrently,
	//as subsequent calls behave differently.
	private synchronized void prepareSharedFields()
	{
		if (m_sharedFields != null)
			return;

		m_sharedFields = new ArrayList<SharedField<?>>();

		ArrayList<Class<?>> classWalk = new ArrayList<Class<?>>();

		for (Class<?> c = this.getClass(); c != null && c != Object.class; c = c.getSuperclass())
			classWalk.add(c);

		// Walked from reverse order so if X extends Y, but Y is looking for a
		// pair and X is requested, X can act as a pair.
		// if walked forward field IDs would be out of sync
		Collections.reverse(classWalk);

		int id = 0;

		for (Class<?> c : classWalk)
		{
			TreeMap<String, SharedField<?>> networkShared = new TreeMap<String, SharedField<?>>();

			for (Field f : c.getDeclaredFields())
			{
				try
				{
					f.setAccessible(true);

					if (SharedField.class.isAssignableFrom(f.getType()))
					{
						SharedField<?> sharedObject = (SharedField<?>) f.get(this);

						networkShared.put(c.getCanonicalName() + "." + f.getName(), sharedObject);
					}
				} catch (IllegalArgumentException | IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}
			}

			for (SharedField<?> shared : networkShared.values())
			{
				shared.setId(id);
				id++;
				m_sharedFields.add(shared);
			}
		}
	}

	protected SharedEntity getParent()
	{
		return m_parent;
	}

	private SharedField<?> getField(int id)
	{
		prepareSharedFields();

		for (SharedField<?> entry : m_sharedFields)
		{
			if (entry.getId() == id)
				return entry;
		}

		return null;
	}

	protected final void enqueueFieldSync(Communicator sender, int fieldId, Object value) throws InvalidFieldIdException, PolicyViolationException
	{
		SharedField<?> field = getField(fieldId);

		if (field == null)
			throw new InvalidFieldIdException(this.getClass(), fieldId);

		SharePolicy policy = field.getPolicy();

		if (!policy.canRead(sender.isOwned(this)))
			throw new PolicyViolationException(field);

		m_fieldSyncQueue.add(new FieldSynchronizationQuery(sender, fieldId, value));
	}

	protected final void enqueueMessage(Communicator sender, Object message)
	{
		m_messageQueue.add(new MessageSynchronizationQuery(sender, message));
	}

	protected final void enqueueLogicTask(ISynchronousTask task)
	{
		m_syncLogicExecutor.enqueue(task);
	}
	
	private final void snapshot()
	{
		prepareSharedFields();

		for (Communicator listener : m_boundCommunicators)
		{
			for (SharedField<?> entry : m_sharedFields)
			{
				SharePolicy policy = entry.getPolicy();

				if (policy.canWrite(listener.isOwned(this)) && policy.canRead(!listener.isOwned(this)))
				{
					if (entry.isDirty() && entry.get() != null)
						listener.snapshotField(this, entry, entry.get());
				}
			}
		}
	}

	public final void update(int deltaTime)
	{
		doLogic(deltaTime);
		
		m_snapshotTickCount += deltaTime;
		
		if(m_snapshotTickCount >= m_snapshotInterval)
		{
			m_snapshotTickCount = 0;
			snapshot();
		}
	
		m_syncLogicExecutor.execute();
	}
	
	//When InvalidMessageException is thrown, the message which caused the exception is removed from the queue.
	public final void synchronize() throws InvalidMessageException
	{
		ArrayList<MessageSynchronizationQuery> requeue = new ArrayList<>();
		
		for(MessageSynchronizationQuery message; (message = m_messageQueue.poll()) != null; )
		{
			if (!onMessageRecieved(message.getSender(), message.getMessage()))
				requeue.add(message);
		}
		m_messageQueue.addAll(requeue);
		
		for(FieldSynchronizationQuery query; (query = m_fieldSyncQueue.poll()) != null;)
		{
			SharedField<?> sharedField = getField(query.getId());
			synchronizeShared(query.getSender(), sharedField, query.getValue());
		}
	
		doSynchronization();
	}
	
	public final boolean isSharing(SharedEntity networkEntity)
	{
		synchronized(m_children)
		{
			return m_children.contains(networkEntity);
		}
	}

	protected final void share(SharedEntity networkEntity)
	{
		synchronized (m_children)
		{
			networkEntity.m_parent = this;

			m_children.add(networkEntity);

			for (Communicator listener : m_boundCommunicators)
				listener.shareEntity(networkEntity);
		}
	}

	protected final void unshare(SharedEntity networkEntity)
	{
		synchronized (m_children)
		{
			if (!m_children.remove(networkEntity))
				throw new NoSuchElementException();

			for (Communicator listener : m_boundCommunicators)
				listener.unshareEntity(networkEntity);
		}
	}

	protected final void send(Communicator receiver, Object message)
	{
		if (m_boundCommunicators.contains(receiver))
			receiver.enqueueMessage(this, message);
	}

	protected final void send(Object message)
	{
		for (Communicator listener : m_boundCommunicators)
			listener.enqueueMessage(this, message);
	}

	protected void synchronizeShared(Communicator sender, SharedField<?> sharedField, Object value)
	{
		sharedField.m_shared = value;
	}
	
	protected void onCommunicatorBound(Communicator communicator) { }
	protected void onCommunicatorUnbound(Communicator communicator) { }
	
	protected void onEntityShared(SharedEntity e) { }
	protected void onEntityUnshared(SharedEntity e) { }

	protected abstract void doLogic(int deltaTime);
	protected abstract void doSynchronization()  throws InvalidMessageException;
	
	protected abstract boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException;
	
	protected final class SharedField<T>
	{
		private static final int INVALID_ID = -1;

		private SharePolicy m_policy;
		private Object m_shared;

		private boolean m_isDirty;

		private int m_id;

		public SharedField(SharePolicy policy)
		{
			this(null, policy);
		}

		public SharedField(T value, SharePolicy policy)
		{
			m_id = INVALID_ID;
			m_policy = policy;

			if (value != null)
			{
				m_isDirty = true;
				m_shared = value;
			}
		}

		public SharePolicy getPolicy()
		{
			return m_policy;
		}

		public void setDirty()
		{
			m_isDirty = true;
		}

		protected boolean isDirty()
		{
			boolean dirty = m_isDirty;
			m_isDirty = false;
			return dirty;
		}

		public void set(T value)
		{
			m_shared = value;
		}

		@SuppressWarnings("unchecked")
		public T get()
		{
			return (T) m_shared;
		}

		protected void setId(int id)
		{
			m_id = id;
		}

		public int getId()
		{
			return m_id;
		}
	}

	private class MessageSynchronizationQuery
	{
		private Communicator m_sender;
		private Object m_message;

		public MessageSynchronizationQuery(Communicator sender, Object message)
		{
			m_sender = sender;
			m_message = message;
		}

		public Communicator getSender()
		{
			return m_sender;
		}

		public Object getMessage()
		{
			return m_message;
		}
	}

	private class FieldSynchronizationQuery
	{
		private Communicator m_sender;
		private int m_fieldId;
		private Object m_value;

		public FieldSynchronizationQuery(Communicator sender, int fieldId, Object value)
		{
			m_sender = sender;
			m_value = value;
			m_fieldId = fieldId;
		}

		public Communicator getSender()
		{
			return m_sender;
		}

		public Object getValue()
		{
			return m_value;
		}

		public int getId()
		{
			return m_fieldId;
		}
	}
}
