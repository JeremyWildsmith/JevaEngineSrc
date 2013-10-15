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
package jeva.graphics;

import java.awt.Point;
import java.awt.Rectangle;


public final class Frame
{

	/** The m_src rect. */
	private Rectangle m_srcRect;

	/** The m_delay. */
	private int m_delay;

	/** The m_origin. */
	private Point m_origin;

	
	public Frame(Rectangle srcRect, int delay, Point origin)
	{
		m_delay = delay;

		m_srcRect = new Rectangle(srcRect);

		m_origin = origin;
	}

	
	public Rectangle getSourceRect()
	{
		return m_srcRect;
	}

	
	public long getDelay()
	{
		return m_delay;
	}

	
	public Point getOrigin()
	{
		return m_origin;
	}
}
