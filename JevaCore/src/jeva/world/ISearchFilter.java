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

import jeva.math.Vector2F;

import com.sun.istack.internal.Nullable;

/**
 * The Interface ISearchFilter.
 * 
 * @param <T>
 *            the generic type
 */
public interface ISearchFilter<T>
{

	/**
	 * Gets the search bounds.
	 * 
	 * @return the search bounds
	 */
	public abstract Rectangle getSearchBounds();

	/**
	 * Should include.
	 * 
	 * @param location
	 *            the location
	 * @return true, if successful
	 */
	public abstract boolean shouldInclude(Vector2F location);

	/**
	 * Filter.
	 * 
	 * @param item
	 *            the item
	 * @return the t
	 */
	@Nullable
	public abstract T filter(T item);
}
