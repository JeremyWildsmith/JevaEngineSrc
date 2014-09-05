package io.github.jevaengine.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ValueCollectedWeakHashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>
{
	private HashMap<Key, V> m_hashMap = new HashMap<>();
	private ReferenceQueue<K> m_keyReferenceQueue = new ReferenceQueue<>();
	
	private Queue<V> m_valueCollector;
	
	public ValueCollectedWeakHashMap(Queue<V> valueCollector)
	{
		m_valueCollector = valueCollector;
	}
	
	private void cleanup()
	{
		for(Object key; (key = m_keyReferenceQueue.poll()) != null;)
			m_hashMap.remove(key);
	}
	
	@Override
	public void clear()
	{
		m_hashMap.clear();
	}

	@Override
	public boolean containsKey(Object key)
	{
		cleanup();
		return m_hashMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		cleanup();
		return m_hashMap.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(Object key)
	{
		cleanup();
		return m_hashMap.get(key);
	}

	@Override
	public boolean isEmpty()
	{
		cleanup();
		return m_hashMap.isEmpty();
	}

	@Override
	public Set<K> keySet()
	{
		cleanup();
		HashSet<K> set = new HashSet<K>();
		
		for(Key k : m_hashMap.keySet())
		{
			K key = k.get();
			
			if(key != null)
				set.add(key);
		}
		
		return set;
	}

	@Override
	public V put(K key, V value)
	{
		return m_hashMap.put(new Key(key), value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map)
	{
		for(Map.Entry<? extends K, ? extends V> entry : map.entrySet())
			put(entry.getKey(), entry.getValue());
	}

	@Override
	public V remove(Object key)
	{
		V value = m_hashMap.remove(key);
		
		if(value != null)
			m_valueCollector.add(value);

		return value;
	}

	@Override
	public int size()
	{
		cleanup();
		return m_hashMap.size();
	}

	@Override
	public Collection<V> values()
	{
		cleanup();
		
		return m_hashMap.values();
	}

	private class Key extends WeakReference<K>
	{
		private int m_hashCode = 0;
		public Key(K key)
		{
			super(key, m_keyReferenceQueue);
			m_hashCode = key.hashCode();
		}

		@Override
		public int hashCode()
		{
			return m_hashCode;
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj == this)
				return true;
			
			K key = get();
			
			if(key == null)
				return false;
			else
				return key.equals(obj);
		}
	}
}
