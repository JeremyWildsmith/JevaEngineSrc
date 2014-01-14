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
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;

public interface ISearchFilter<T>
{

	Rect2D getSearchBounds();

	boolean shouldInclude(Vector2F location);

	@Nullable
	T filter(T item);
}
