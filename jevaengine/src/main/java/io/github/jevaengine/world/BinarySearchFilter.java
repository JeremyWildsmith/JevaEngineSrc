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
package io.github.jevaengine.world;

import io.github.jevaengine.math.Rect2D;
import java.awt.Rectangle;
import java.util.NoSuchElementException;

import io.github.jevaengine.math.Vector2F;

public class BinarySearchFilter<T> implements ISearchFilter<T>
{

	enum Operation
	{

		Or,

		And,

		Nand,

		Nor,

		Xor
	}

	private Operation m_operation;

	private ISearchFilter<T> m_filterA;

	private ISearchFilter<T> m_filterB;

	public BinarySearchFilter(ISearchFilter<T> a, ISearchFilter<T> b, Operation operation)
	{
		m_operation = operation;
		m_filterA = a;
		m_filterB = b;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ISearchFilter#getSearchBounds()
	 */
	@Override
	public Rect2D getSearchBounds()
	{
		Rect2D boundsA = m_filterA.getSearchBounds();
		Rect2D boundsB = m_filterB.getSearchBounds();

		return new Rect2D(Math.min(boundsA.x, boundsB.x), Math.min(boundsA.y, boundsB.y), Math.max(boundsA.width, boundsB.width), Math.max(boundsA.height, boundsB.height));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ISearchFilter#shouldInclude(jeva.math.Vector2F)
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
	 * @see io.github.jeremywildsmith.jevaengine.world.ISearchFilter#filter(java.lang.Object)
	 */
	@Override
	public T filter(T item)
	{
		return item;
	}

}
