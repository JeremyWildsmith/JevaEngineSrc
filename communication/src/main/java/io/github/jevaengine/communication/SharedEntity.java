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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.TreeMap;

public abstract class SharedEntity
{
	private ArrayList<SharedField<?>> m_sharedFields;

	private ArrayList<FieldSynchronizationQuery> m_fieldSyncQueue;

	private ArrayList<MessageSynchronizationQuery> m_messageQueue;

	private StaticSet<Communicator> m_boundCommunicators;

	private SharedEntity m_parent;

	private ArrayList<SharedEntity> m_children = new ArrayList<SharedEntity>();

	public SharedEntity()
	{
		m_fieldSyncQueue = new ArrayList<FieldSynchronizationQuery>();

		m_messageQueue = new ArrayList<MessageSynchronizationQuery>();

		m_boundCommunicators = new StaticSet<Communicator>();
	}

	void bindCommunicator(Communicator listener) throws IOException, ShareEntityException, PolicyViolationException
	{
		synchronized (m_children)
		{
			m_boundCommunicators.add(listener);

			for (SharedEntity e : m_children)
				listener.shareEntity(e);
		}
	}

	void unbindCommunicator(Communicator listener) throws IOException
	{
		synchronized (m_children)
		{
			m_boundCommunicators.remove(listener);

			for (SharedEntity e : m_children)
				listener.unshareEntity(e);
		}
	}

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
		synchronized (m_fieldSyncQueue)
		{
			SharedField<?> field = getField(fieldId);

			if (field == null)
				throw new InvalidFieldIdException(this.getClass(), fieldId);

			SharePolicy policy = field.getPolicy();

			if (!policy.canRead(sender.isOwned(this)))
				throw new PolicyViolationException(field);

			m_fieldSyncQueue.add(new FieldSynchronizationQuery(sender, fieldId, value));
		}
	}

	protected final void enqueueMessage(Communicator sender, Object message)
	{
		synchronized (m_messageQueue)
		{
			m_messageQueue.add(new MessageSynchronizationQuery(sender, message));
		}
	}

	protected final void snapshot()
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

	public final void synchronize() throws InvalidMessageException
	{
		synchronized (m_messageQueue)
		{
			ListIterator<MessageSynchronizationQuery> it = m_messageQueue.listIterator();

			while (it.hasNext())
			{
				MessageSynchronizationQuery message = it.next();

				try
				{
					if (onMessageRecieved(message.getSender(), message.getMessage()))
						it.remove();

				} catch (InvalidMessageException e)
				{
					// If the message is invalid, remove it from the queue and
					// rethrow
					it.remove();
					throw e;
				}
			}
		}

		synchronized (m_fieldSyncQueue)
		{
			ListIterator<FieldSynchronizationQuery> it = m_fieldSyncQueue.listIterator();

			while (it.hasNext())
			{
				FieldSynchronizationQuery query = it.next();

				SharedField<?> sharedField = getField(query.getId());

				if (synchronizeShared(query.getSender(), sharedField, query.getValue()))
					it.remove();
			}
		}

		for (SharedEntity e : m_children)
			e.synchronize();
	}

	protected final void share(SharedEntity networkEntity) throws IOException, ShareEntityException, PolicyViolationException
	{
		synchronized (m_children)
		{
			networkEntity.m_parent = this;

			m_children.add(networkEntity);

			for (Communicator listener : m_boundCommunicators)
				listener.shareEntity(networkEntity);
		}
	}

	protected final void unshare(SharedEntity networkEntity) throws IOException
	{
		synchronized (m_children)
		{
			if (!m_children.remove(networkEntity))
				throw new NoSuchElementException();

			networkEntity.m_parent = null;

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

	protected boolean synchronizeShared(Communicator sender, SharedField<?> sharedField, Object value)
	{
		sharedField.m_shared = value;
		return true;
	}

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
