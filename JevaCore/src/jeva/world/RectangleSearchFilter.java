package jeva.world;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;

import jeva.math.Vector2F;

/**
 * The Class RectangleSearchFilter.
 * 
 * @param <T>
 *            the generic type
 */
public class RectangleSearchFilter<T> implements ISearchFilter<T>
{

	/** The m_src rect. */
	private Rectangle2D.Float m_srcRect;

	/**
	 * Instantiates a new rectangle search filter.
	 * 
	 * @param rect
	 *            the rect
	 */
	public RectangleSearchFilter(Rectangle2D.Float rect)
	{
		m_srcRect = rect;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#getSearchBounds()
	 */
	@Override
	public final Rectangle getSearchBounds()
	{
		return m_srcRect.getBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#shouldInclude(jeva.math.Vector2F)
	 */
	@Override
	public boolean shouldInclude(Vector2F location)
	{
		return m_srcRect.contains(location.x, location.y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#filter(java.lang.Object)
	 */
	@Override
	public T filter(T o)
	{
		return o;
	}
}
