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


public interface ISearchFilter<T>
{

	
	public abstract Rectangle getSearchBounds();

	
	public abstract boolean shouldInclude(Vector2F location);

	
	@Nullable
	public abstract T filter(T item);
}
