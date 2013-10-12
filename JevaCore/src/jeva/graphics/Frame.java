/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.graphics;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * The Class Frame.
 * 
 * @author Jeremy. A. W
 */
public final class Frame
{

	/** The m_src rect. */
	private Rectangle m_srcRect;

	/** The m_delay. */
	private int m_delay;

	/** The m_origin. */
	private Point m_origin;

	/**
	 * Instantiates a new frame.
	 * 
	 * @param srcRect
	 *            the src rect
	 * @param delay
	 *            the delay
	 * @param origin
	 *            the origin
	 */
	public Frame(Rectangle srcRect, int delay, Point origin)
	{
		m_delay = delay;

		m_srcRect = new Rectangle(srcRect);

		m_origin = origin;
	}

	/**
	 * Gets the source rect.
	 * 
	 * @return the source rect
	 */
	public Rectangle getSourceRect()
	{
		return m_srcRect;
	}

	/**
	 * Gets the delay.
	 * 
	 * @return the delay
	 */
	public long getDelay()
	{
		return m_delay;
	}

	/**
	 * Gets the origin.
	 * 
	 * @return the origin
	 */
	public Point getOrigin()
	{
		return m_origin;
	}
}
