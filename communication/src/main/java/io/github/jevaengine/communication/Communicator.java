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

import io.github.jevaengine.communication.SharedEntity.SharedField;
import io.github.jevaengine.communication.Snapshot.FieldSnapshot;
import io.github.jevaengine.communication.Snapshot.MessageSnapshot;
import io.github.jevaengine.util.StaticSet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Communicator
{
	private static final int PARENT_ID_ROOT = -1;
	
	private HashMap<String, Class<?>> m_registeredClasses;

	private ConcurrentHashMap<EntityId, SharedEntity> m_pairs;

	private Snapshot m_workingSnapshot;

	private RemoteCommunicator m_remote;

	private long m_nextId;

	private Observers m_observers = new Observers();

	public Communicator()
	{
		m_registeredClasses = new HashMap<String, Class<?>>();
		m_pairs = new ConcurrentHashMap<EntityId, SharedEntity>();
		m_workingSnapshot = new Snapshot();

		m_nextId = 0;
	}

	private EntityId getEntityId(SharedEntity entity)
	{
		for (Map.Entry<EntityId, SharedEntity> entry : m_pairs.entrySet())
		{
			if (entry.getValue() == entity)
				return entry.getKey();
		}

		throw new NoSuchElementException();
	}

	private void routeSnapshot(Snapshot snapshot) throws SnapshotSynchronizationException
	{
		for (MessageSnapshot msg : snapshot.getMessages())
		{
			// If the sender owned the item, I don't and vise-versa, so
			// ownership must be reversed
			EntityId id = new EntityId(!msg.getSender().isOwned(), msg.getSender().getId());

			SharedEntity pair = m_pairs.get(id);

			if (pair != null)
				pair.enqueueMessage(this, msg.m_message);
		}

		for (FieldSnapshot field : snapshot.getFields())
		{
			// If the sender owned the item, I don't vise-versa, so ownership
			// must be reversed
			EntityId id = new EntityId(!field.getSender().isOwned(), field.getSender().getId());

			// If I don't have the ID, it means the snapshot is old, and no
			// longer relevant, ignore.
			if (m_pairs.containsKey(id))
			{
				try
				{
					m_pairs.get(id).enqueueFieldSync(this, field.getFieldId(), field.getValue());
				} catch (InvalidFieldIdException ex)
				{
					throw new SnapshotSynchronizationException(ex);
				} catch (PolicyViolationException ex)
				{
					throw new SnapshotSynchronizationException(ex);
				}
			}
		}
	}

	private void pairEntity(long id, SharedEntity entity)
	{
		m_pairs.put(new EntityId(false, id), entity);
	}

	protected final void createPair(long parentId, long id, String className)
	{
		if (!m_registeredClasses.containsKey(className))
			throw new ShareEntityException("No registered class under name: " + className);

		Class<?> pairClass = m_registeredClasses.get(className);

		SharedClass shared = pairClass.getAnnotation(SharedClass.class);

		if (shared == null)
			throw new NoSharePolicyException();
		else if (!shared.policy().canWrite(!isServer()) || !shared.policy().canRead(isServer()))
			throw new PolicyViolationException(className, shared.policy());

		SharedEntity entity = instantiatePair(pairClass);

		pairEntity(id, entity);

		entity.bindCommunicator(this);
		
		if(parentId != PARENT_ID_ROOT)
		{
			SharedEntity parent = m_pairs.get(new EntityId(false, parentId));
			
			if(parent == null)
				throw new ShareEntityException("Parent entity id was invalid.");
			
			parent.childEntityShared(entity);
		}
		
		m_observers.entityShared(entity);
	}

	protected SharedEntity instantiatePair(Class<?> entityClass) throws ShareEntityException
	{
		try
		{
			return (SharedEntity) entityClass.getConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			throw new ShareEntityException("Unable to instantiate pair " + entityClass.getCanonicalName() + ": " + e.toString());
		}
	}

	protected final void pairDestroyed(long remoteId) throws PolicyViolationException, IOException
	{
		EntityId id = new EntityId(false, remoteId);

		if (!m_pairs.containsKey(id))
			return;

		Class<?> pairClass = m_pairs.get(id).getClass();
		SharedClass shared = pairClass.getAnnotation(SharedClass.class);

		if (shared == null)
			throw new NoSharePolicyException();
		else if (shared.policy().canWrite(false))
			throw new PolicyViolationException(pairClass.getCanonicalName(), shared.policy());

		SharedEntity entity = m_pairs.remove(id);
		entity.unbindCommunicator(this);
		
		SharedEntity parent = entity.getParent();
		
		if(parent != null)
			parent.childEntityUnshared(entity);
		
		m_observers.entityUnshared(entity);
	}
	
	final void snapshotField(SharedEntity networkEntity, SharedField<?> field, Object value)
	{
		synchronized (m_workingSnapshot)
		{
			m_workingSnapshot.snapshotField(getEntityId(networkEntity), field.getId(), value);
		}
	}

	final void enqueueMessage(SharedEntity networkEntity, Object message)
	{
		synchronized (m_workingSnapshot)
		{
			m_workingSnapshot.enqueueMessage(getEntityId(networkEntity), message);
		}
	}

	protected final void snapshot() throws IOException, SnapshotSynchronizationException
	{
		synchronized (m_workingSnapshot)
		{
			if (!m_workingSnapshot.isEmpty())
			{
				if (m_remote == null)
					throw new UnboundCommunicatorException();

				m_remote.remoteSnapshot(m_workingSnapshot);
			}
			m_workingSnapshot.clear();
		}
	}

	protected final <T extends SharedEntity> void registerClass(Class<T> networkClass)
	{
		SharedClass annotation = networkClass.getAnnotation(SharedClass.class);

		if (annotation == null)
			throw new NoSharePolicyException();

		if (!SharedEntity.class.isAssignableFrom(networkClass))
			throw new ClassRegistrationException("Class must extend SharedEntity");

		m_registeredClasses.put(annotation.name(), networkClass);
	}

	public final void shareEntity(SharedEntity networkEntity) throws ShareEntityException
	{
		if (m_pairs.containsValue(networkEntity))
			return;

		if (!m_registeredClasses.values().contains(networkEntity.getClass()))
			throw new ShareEntityException("Failed to share entity, class not registered: " + networkEntity.getClass().getCanonicalName());

		SharedClass shared = networkEntity.getClass().getAnnotation(SharedClass.class);

		if (shared == null)
			throw new NoSharePolicyException();
		else if (!shared.policy().canWrite(isServer()) || !shared.policy().canRead(!isServer()))
			throw new PolicyViolationException(networkEntity, shared.policy());

		if (m_remote == null)
			throw new UnboundCommunicatorException();

		EntityId id = new EntityId(true, m_nextId++);
		m_pairs.put(id, networkEntity);

		SharedEntity parent = networkEntity.getParent();
		if(parent != null)
		{
			EntityId parentId = getEntityId(parent);
			m_remote.remoteQueryPair(parentId.getId(), id.getId(), shared.name());
		}else
			m_remote.remoteQueryPair(PARENT_ID_ROOT, id.getId(), shared.name());

		networkEntity.bindCommunicator(this);
		m_observers.entityShared(networkEntity);
	}

	public final boolean unshareEntity(SharedEntity networkEntity)
	{
		if (m_remote == null)
			throw new UnboundCommunicatorException();
		
		ArrayList<EntityId> garbage = new ArrayList<EntityId>();

		for (Map.Entry<EntityId, SharedEntity> entry : m_pairs.entrySet())
		{
			if (entry.getKey().isOwned() && entry.getValue() == networkEntity)
			{
				garbage.add(entry.getKey());
			}
		}

		for (EntityId id : garbage)
		{
			SharedEntity garbageEntity = m_pairs.get(id);

			m_observers.entityUnshared(m_pairs.get(id));
			
			garbageEntity.unbindCommunicator(this);
			m_remote.remoteDestroyPair(id.getId());
			
			m_pairs.remove(id);
		}

		return garbage.size() > 0;
	}

	public boolean isOwned(SharedEntity sharedEntity)
	{
		for (Map.Entry<EntityId, SharedEntity> entry : m_pairs.entrySet())
		{
			if (entry.getValue() == sharedEntity)
				return entry.getKey().isOwned();
		}

		return false;
	}

	public final boolean isBound()
	{
		return m_remote != null;
	}

	public final void bind(RemoteCommunicator remote)
	{
		if (m_remote != null)
			throw new CommunicatorAlreadyBoundException();

		m_remote = remote;
		remote.bind(this);
	}

	public final void unbind()
	{
		if (m_remote == null)
			throw new UnboundCommunicatorException();

		while (!m_pairs.isEmpty())
		{
			Entry<EntityId, SharedEntity> next = m_pairs.entrySet().iterator().next();
			
			// Removing a listener will cause the corresponding pair to
			// remove pairs shared through it, thus
			// we must iterate through pairs assuming that the collection
			// may change
			// every time we remove a listener.
			m_observers.entityUnshared(next.getValue());
			next.getValue().unbindCommunicator(this);
			m_pairs.remove(next.getKey());
		}

		m_workingSnapshot.clear();

		m_remote.unbind();

		m_remote = null;
	}

	public void remoteDestroyPair(long id) throws IOException
	{
		// The remote has destroyed a pair with the ID 'id'
		try
		{
			pairDestroyed(id);
		} catch (PolicyViolationException e)
		{
			throw new IOException(e);
		}
	}

	public void remoteQueryPair(long parent, long id, String className)
	{
		// The remote has requested I create a pair with the specified class
		// name.
		createPair(parent, id, className);
	}

	public void remoteSnapshot(Snapshot snapshot) throws IOException, SnapshotSynchronizationException
	{
		routeSnapshot(snapshot);
	}

	public void addObserver(ICommunicatorObserver o)
	{
		m_observers.add(o);
	}

	public void removeObserver(ICommunicatorObserver o)
	{
		m_observers.remove(o);
	}

	public final void poll()
	{
		m_observers.poll();
	}

	protected abstract boolean isServer();

	private static class Observers extends StaticSet<ICommunicatorObserver>
	{
		private StaticSet<Runnable> m_events = new StaticSet<Runnable>();

		private synchronized void poll()
		{
			for (Runnable r : m_events)
				r.run();

			m_events.clear();
		}

		public synchronized void entityShared(final SharedEntity e)
		{
			m_events.add(new Runnable() {
				@Override
				public void run()
				{
					for (ICommunicatorObserver o : Observers.this)
						o.entityShared(e);
				}
			});
		}

		public synchronized void entityUnshared(final SharedEntity e)
		{
			m_events.add(new Runnable() {
				@Override
				public void run()
				{
					for (ICommunicatorObserver o : Observers.this)
						o.entityUnshared(e);
				}
			});
		}
	}

	public interface ICommunicatorObserver
	{
		void entityShared(SharedEntity e);

		void entityUnshared(SharedEntity e);
	}

	public abstract static class RemoteCommunicator
	{
		private Communicator m_listener;

		public RemoteCommunicator()
		{

		}

		private void bind(Communicator listener)
		{
			if (m_listener != null)
				throw new CommunicatorAlreadyBoundException();

			m_listener = listener;
		}

		private void unbind()
		{
			m_listener = null;
		}

		protected final Communicator getListener()
		{
			if (m_listener == null)
				throw new UnboundCommunicatorException();

			return m_listener;
		}
		
		protected abstract void onBind();

		protected abstract void onUnbind();

		public abstract void remoteDestroyPair(long id);
		public abstract void remoteQueryPair(long parent, long id, String className);

		public abstract void remoteSnapshot(Snapshot snapshot);
	}
}
