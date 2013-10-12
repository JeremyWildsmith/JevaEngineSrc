package jeva.world;

import java.awt.Rectangle;

import jeva.math.Matrix2X2;
import jeva.math.Vector2F;

/**
 * The Class TriangleSearchFilter.
 * 
 * @param <T>
 *            the generic type
 */
public class TriangleSearchFilter<T> implements ISearchFilter<T>
{

	/** The m_world to barycentric. */
	private Matrix2X2 m_worldToBarycentric;

	/** The m_vertice. */
	private Vector2F[] m_vertice;

	/**
	 * Instantiates a new triangle search filter.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param c
	 *            the c
	 */
	public TriangleSearchFilter(Vector2F a, Vector2F b, Vector2F c)
	{

		m_vertice = new Vector2F[]
		{ a, b, c };

		m_worldToBarycentric = new Matrix2X2(m_vertice[2].x - m_vertice[0].x, m_vertice[1].x - m_vertice[0].y, m_vertice[2].y - m_vertice[0].y, m_vertice[1].y - m_vertice[0].y).inverse();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#getSearchBounds()
	 */
	@Override
	public final Rectangle getSearchBounds()
	{
		float xMin = Math.min(Math.min(m_vertice[0].x, m_vertice[1].x), m_vertice[2].x);
		float xMax = Math.max(Math.max(m_vertice[0].x, m_vertice[1].x), m_vertice[2].x);

		float yMin = Math.min(Math.min(m_vertice[0].y, m_vertice[1].y), m_vertice[2].y);
		float yMax = Math.max(Math.max(m_vertice[0].y, m_vertice[1].y), m_vertice[2].y);

		return new Rectangle((int) Math.floor(xMin), (int) Math.floor(yMin), (int) Math.ceil(xMax - xMin), (int) Math.ceil(yMax - yMin));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#shouldInclude(jeva.math.Vector2F)
	 */
	@Override
	public final boolean shouldInclude(Vector2F location)
	{
		Vector2F v = m_worldToBarycentric.dot(location.difference(m_vertice[0]));

		return (v.x + v.y <= 1.0F && v.x >= 0.0F && v.y >= 0.0F);
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
