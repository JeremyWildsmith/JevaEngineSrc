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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.graphics;

import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.util.Nullable;

public final class Frame
{
	private final Rect2D m_srcRect;
	private final int m_delay;
	private final Vector2D m_origin;
	private final String m_event;
	
	public Frame(Rect2D srcRect, int delay, Vector2D origin, @Nullable String event)
	{
		m_delay = delay;
		m_srcRect = new Rect2D(srcRect);
		m_origin = new Vector2D(origin);
		m_event = event;
	}

	public Frame(Rect2D srcRect, int delay, Vector2D origin)
	{
		this(srcRect, delay, origin, null);
	}
	
	public Rect2D getSourceRect()
	{
		return new Rect2D(m_srcRect);
	}

	public long getDelay()
	{
		return m_delay;
	}

	public Vector2D getOrigin()
	{
		return new Vector2D(m_origin);
	}
	
	@Nullable
	public String getEvent()
	{
		return m_event;
	}
}
