package jeva.world;

import java.awt.Rectangle;
import java.util.NoSuchElementException;

import jeva.math.Vector2F;

/**
 * The Class BinarySearchFilter.
 * 
 * @param <T>
 *            the generic type
 */
public class BinarySearchFilter<T> implements ISearchFilter<T>
{

	/**
	 * The Enum Operation.
	 */
	enum Operation
	{

		/** The Or. */
		Or,
		/** The And. */
		And,
		/** The Nand. */
		Nand,
		/** The Nor. */
		Nor,
		/** The Xor. */
		Xor
	}

	/** The m_operation. */
	private Operation m_operation;

	/** The m_filter a. */
	private ISearchFilter<T> m_filterA;

	/** The m_filter b. */
	private ISearchFilter<T> m_filterB;

	/**
	 * Instantiates a new binary search filter.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @param operation
	 *            the operation
	 */
	public BinarySearchFilter(ISearchFilter<T> a, ISearchFilter<T> b, Operation operation)
	{
		m_operation = operation;
		m_filterA = a;
		m_filterB = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#getSearchBounds()
	 */
	@Override
	public Rectangle getSearchBounds()
	{
		Rectangle boundsA = m_filterA.getSearchBounds();
		Rectangle boundsB = m_filterB.getSearchBounds();

		return new Rectangle(Math.min(boundsA.x, boundsB.x), Math.min(boundsA.y, boundsB.y), Math.max(boundsA.width, boundsB.width), Math.max(boundsA.height, boundsB.height));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#shouldInclude(jeva.math.Vector2F)
	 */
	@Override
	public final boolean shouldInclude(Vector2F location)
	{
		switch (m_operation)
		{
		case And:
			return m_filterA.shouldInclude(location) && m_filterB.shouldInclude(location);
		case Or:
			return m_filterA.shouldInclude(location) || m_filterB.shouldInclude(location);
		case Nand:
			return !(m_filterA.shouldInclude(location) && m_filterB.shouldInclude(location));
		case Nor:
			return !(m_filterA.shouldInclude(location) || m_filterB.shouldInclude(location));
		case Xor:
			return (m_filterA.shouldInclude(location) ^ m_filterB.shouldInclude(location));
		default:
			throw new NoSuchElementException();
		}
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
