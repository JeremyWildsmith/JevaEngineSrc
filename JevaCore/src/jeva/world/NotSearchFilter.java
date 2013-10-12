package jeva.world;

import java.awt.Rectangle;

import jeva.math.Vector2F;

/**
 * The Class NotSearchFilter.
 * 
 * @param <T>
 *            the generic type
 */
public class NotSearchFilter<T> implements ISearchFilter<T>
{

	/** The m_filter. */
	private ISearchFilter<T> m_filter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#getSearchBounds()
	 */
	@Override
	public Rectangle getSearchBounds()
	{
		return m_filter.getSearchBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#shouldInclude(jeva.math.Vector2F)
	 */
	@Override
	public boolean shouldInclude(Vector2F location)
	{
		return !m_filter.shouldInclude(location);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#filter(java.lang.Object)
	 */
	@Override
	public T filter(T item)
	{
		return item;
	}

}
