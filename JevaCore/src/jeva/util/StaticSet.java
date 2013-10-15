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
package jeva.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;


public class StaticSet<T> implements Iterable<T>
{

	/** The m_set. */
	private ArrayList<T> m_set = new ArrayList<T>();

	/** The m_mutations. */
	private ArrayList<Runnable> m_mutations = new ArrayList<Runnable>();

	
	private void cleanup()
	{
		for (Runnable r : m_mutations)
			r.run();

		m_mutations.clear();
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
	}

	
	public final boolean contains(T element)
	{
		cleanup();

		return m_set.contains(element);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public final Iterator<T> iterator()
	{
		return new StaticSetIterator();
	}

	
	public final class StaticSetIterator implements Iterator<T>
	{

		/** The m_index. */
		int m_index = -1;

		
		private StaticSetIterator()
		{
			cleanup();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return m_index < m_set.size() - 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException();
			}

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
}
