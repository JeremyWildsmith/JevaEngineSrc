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
package jeva.graphics.ui;

import java.awt.Rectangle;

import jeva.graphics.IRenderable;
import jeva.joystick.InputManager;
import jeva.math.Vector2D;

public abstract class Control implements IRenderable
{

	/** The m_parent. */
	private Control m_parent;

	/** The m_style. */
	private UIStyle m_style;

	/** The m_location. */
	private Vector2D m_location;

	/** The m_is visible. */
	private boolean m_isVisible;

	/**
	 * Instantiates a new control.
	 */
	public Control()
	{
		m_location = new Vector2D();
		m_isVisible = true;
	}

	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public final Vector2D getLocation()
	{
		return m_location;
	}

	/**
	 * Sets the location.
	 * 
	 * @param location
	 *            the new location
	 */
	public final void setLocation(Vector2D location)
	{
		m_location = location;
	}

	/**
	 * Gets the absolute location.
	 * 
	 * @return the absolute location
	 */
	public final Vector2D getAbsoluteLocation()
	{
		if (m_parent != null)
			return m_location.add(m_parent.getAbsoluteLocation());
		else
			return m_location;
	}

	/**
	 * Gets the style.
	 * 
	 * @return the style
	 */
	public final UIStyle getStyle()
	{
		return m_style;
	}

	/**
	 * Sets the style.
	 * 
	 * @param style
	 *            the new style
	 */
	public final void setStyle(UIStyle style)
	{
		m_style = style;
		onStyleChanged();
	}

	/**
	 * Sets the parent.
	 * 
	 * @param parent
	 *            the new parent
	 */
	public final void setParent(Control parent)
	{
		m_parent = parent;

		if (m_parent != null && m_style == null)
			setStyle(m_parent.getStyle());
	}

	/**
	 * Gets the parent.
	 * 
	 * @return the parent
	 */
	public final Control getParent()
	{
		return m_parent;
	}

	/**
	 * Checks if is visible.
	 * 
	 * @return true, if is visible
	 */
	public final boolean isVisible()
	{
		if (m_parent != null)
			return m_parent.isVisible() && m_isVisible;

		return m_isVisible;
	}

	/**
	 * Sets the visible.
	 * 
	 * @param isVisible
	 *            the new visible
	 */
	public final void setVisible(boolean isVisible)
	{
		if (m_parent != null && isVisible)
			m_parent.setVisible(isVisible);

		m_isVisible = isVisible;
	}

	/**
	 * On style changed.
	 */
	protected void onStyleChanged()
	{
	}

	/**
	 * On enter.
	 */
	protected void onEnter()
	{
	}

	/**
	 * On leave.
	 */
	protected void onLeave()
	{
	}

	/**
	 * On mouse event.
	 * 
	 * @param mouseEvent
	 *            the mouse event
	 */
	public abstract void onMouseEvent(InputManager.InputMouseEvent mouseEvent);

	/**
	 * On key event.
	 * 
	 * @param keyEvent
	 *            the key event
	 */
	public abstract void onKeyEvent(InputManager.InputKeyEvent keyEvent);

	/**
	 * Gets the bounds.
	 * 
	 * @return the bounds
	 */
	public abstract Rectangle getBounds();

	/**
	 * Update.
	 * 
	 * @param deltaTime
	 *            the delta time
	 */
	public abstract void update(int deltaTime);
}
