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
package jeva.world;

import java.awt.Rectangle;
import java.awt.Shape;

import jeva.math.Matrix2X2;
import jeva.math.Vector2F;

/**
 * The Class TransformShapeSearchFilter.
 * 
 * @param <T>
 *            the generic type
 */
public class TransformShapeSearchFilter<T> implements ISearchFilter<T>
{

	/** The m_shape. */
	private Shape m_shape;

	/** The m_transform. */
	private Matrix2X2 m_transform;

	/**
	 * Instantiates a new transform shape search filter.
	 * 
	 * @param transform
	 *            the transform
	 * @param shape
	 *            the shape
	 */
	public TransformShapeSearchFilter(Matrix2X2 transform, Shape shape)
	{
		m_transform = transform;
		m_shape = shape;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#getSearchBounds()
	 */
	@Override
	public Rectangle getSearchBounds()
	{
		Rectangle bounds = m_shape.getBounds();

		Matrix2X2 inverse = m_transform.inverse();

		Vector2F tl = inverse.dot(new Vector2F(bounds.x, bounds.y));
		Vector2F tr = inverse.dot(new Vector2F(bounds.x + bounds.width, bounds.y));
		Vector2F bl = inverse.dot(new Vector2F(bounds.x, bounds.y + bounds.height));
		Vector2F br = inverse.dot(new Vector2F(bounds.x + bounds.width, bounds.y + bounds.height));

		return new Rectangle((int) (tl.x), (int) (tr.y), (int) (br.x - tl.x), (int) (bl.y - tr.y));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ISearchFilter#shouldInclude(jeva.math.Vector2F)
	 */
	@Override
	public boolean shouldInclude(Vector2F location)
	{
		Vector2F transformedLocation = m_transform.dot(location);

		return m_shape.contains(transformedLocation.x, transformedLocation.y);
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
