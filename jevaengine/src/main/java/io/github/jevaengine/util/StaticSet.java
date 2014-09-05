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
package io.github.jevaengine.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class StaticSet<T> implements Iterable<T>
{
	private ArrayList<T> m_set = new ArrayList<T>();
	private ArrayList<T> m_uncommitedNegative = new ArrayList<T>();
	private ArrayList<T> m_uncommitedPositive = new ArrayList<T>();
	
	private ArrayList<Runnable> m_mutations = new ArrayList<>();

	private int m_mutationCount = 0;
	
	private void cleanup()
	{
		if(m_mutations.isEmpty())
			return;
		
		m_mutationCount++;
		
		for (Runnable r : m_mutations)
			r.run();

		m_mutations.clear();
		m_uncommitedNegative.clear();
		m_uncommitedPositive.clear();
	}

	public final void add(final T item)
	{
		m_mutations.add(new Runnable()
		{

			@Override
			public void run()
			{
				m_set.add(item);
			}
		});
		
		m_uncommitedPositive.add(item);
	}

	public final void add(final int index, final T item)
	{
		m_mutations.add(new Runnable()
		{
			@Override
			public void run()
			{
				m_set.add(index, item);
			}
		});
		
		m_uncommitedPositive.add(item);
	}

	public final void remove(final T item)
	{
		m_mutations.add(new Runnable()
		{

			@Override
			public void run()
			{
				m_set.remove(item);
			}
		});
		

		m_uncommitedNegative.remove(item);
	}

	public final boolean contains(T element)
	{
		int current = Collections.frequency(m_set, element);
		int positive = Collections.frequency(m_uncommitedPositive, element);
		int negative = Collections.frequency(m_uncommitedNegative, element);
	
		return current + positive + negative > 0;
	
	}

	public final void clear()
	{
		m_mutations.add(new Runnable()
		{

			@Override
			public void run()
			{
				m_set.clear();
			}
		});
	}

	public final T get(int index)
	{
		cleanup();

		return m_set.get(index);
	}

	public final int size()
	{
		cleanup();

		return m_set.size();
	}

	public final T[] toArray(T buffer[])
	{
		cleanup();

		return m_set.toArray(buffer);
	}

	public final boolean isEmpty()
	{
		cleanup();

		return m_set.isEmpty();
	}

	@Override
	public final Iterator<T> iterator()
	{
		return new StaticSetIterator();
	}

	public final class StaticSetIterator implements Iterator<T>
	{
		private int m_mutationId;
		private int m_index = -1;

		private StaticSetIterator()
		{
			cleanup();
			m_mutationId = m_mutationCount;
		}

		@Override
		public boolean hasNext()
		{
			if(m_mutationCount != m_mutationId)
				throw new SetMutatationsCommitedException();
			
			return m_index < m_set.size() - 1;
		}

		@Override
		public T next()
		{
			if(m_mutationCount != m_mutationId)
				throw new SetMutatationsCommitedException();

			if (!hasNext())
				throw new NoSuchElementException();

			return m_set.get(++m_index);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
	
	public static final class SetMutatationsCommitedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		private SetMutatationsCommitedException() { }
	}
}
