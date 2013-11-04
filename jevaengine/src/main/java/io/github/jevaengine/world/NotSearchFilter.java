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

import java.awt.Rectangle;

import io.github.jevaengine.math.Vector2F;

public class NotSearchFilter<T> implements ISearchFilter<T>
{

	private ISearchFilter<T> m_filter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ISearchFilter#getSearchBounds()
	 */
	@Override
	public Rectangle getSearchBounds()
	{
		return m_filter.getSearchBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ISearchFilter#shouldInclude(jeva.math.Vector2F)
	 */
	@Override
	public boolean shouldInclude(Vector2F location)
	{
		return !m_filter.shouldInclude(location);
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
