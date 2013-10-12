package jeva.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The Class StaticSet.
 * 
 * @param <T>
 *            the generic type
 */
public class StaticSet<T> implements Iterable<T>
{

	/** The m_set. */
	private ArrayList<T> m_set = new ArrayList<T>();

	/** The m_mutations. */
	private ArrayList<Runnable> m_mutations = new ArrayList<Runnable>();

	/**
	 * Cleanup.
	 */
	private void cleanup()
	{
		for (Runnable r : m_mutations)
			r.run();

		m_mutations.clear();
	}

	/**
	 * Adds the.
	 * 
	 * @param item
	 *            the item
	 */
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

	/**
	 * Adds the.
	 * 
	 * @param index
	 *            the index
	 * @param item
	 *            the item
	 */
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

	/**
	 * Removes the.
	 * 
	 * @param item
	 *            the item
	 */
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

	/**
	 * Contains.
	 * 
	 * @param element
	 *            the element
	 * @return true, if successful
	 */
	public final boolean contains(T element)
	{
		cleanup();

		return m_set.contains(element);
	}

	/**
	 * Clear.
	 */
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

	/**
	 * Gets the.
	 * 
	 * @param index
	 *            the index
	 * @return the t
	 */
	public final T get(int index)
	{
		cleanup();

		return m_set.get(index);
	}

	/**
	 * Size.
	 * 
	 * @return the int
	 */
	public final int size()
	{
		cleanup();

		return m_set.size();
	}

	/**
	 * To array.
	 * 
	 * @param buffer
	 *            the buffer
	 * @return the t[]
	 */
	public final T[] toArray(T buffer[])
	{
		cleanup();

		return m_set.toArray(buffer);
	}

	/**
	 * Checks if is empty.
	 * 
	 * @return true, if is empty
	 */
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

	/**
	 * The Class StaticSetIterator.
	 */
	public final class StaticSetIterator implements Iterator<T>
	{

		/** The m_index. */
		int m_index = -1;

		/**
		 * Instantiates a new static set iterator.
		 */
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
