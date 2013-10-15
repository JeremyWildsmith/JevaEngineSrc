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

	
	public Control()
	{
		m_location = new Vector2D();
		m_isVisible = true;
	}

	
	public final Vector2D getLocation()
	{
		return m_location;
	}

	
	public final void setLocation(Vector2D location)
	{
		m_location = location;
	}

	
	public final Vector2D getAbsoluteLocation()
	{
		if (m_parent != null)
			return m_location.add(m_parent.getAbsoluteLocation());
		else
			return m_location;
	}

	
	public final UIStyle getStyle()
	{
		return m_style;
	}

	
	public final void setStyle(UIStyle style)
	{
		m_style = style;
		onStyleChanged();
	}

	
	public final void setParent(Control parent)
	{
		m_parent = parent;

		if (m_parent != null && m_style == null)
			setStyle(m_parent.getStyle());
	}

	
	public final Control getParent()
	{
		return m_parent;
	}

	
	public final boolean isVisible()
	{
		if (m_parent != null)
			return m_parent.isVisible() && m_isVisible;

		return m_isVisible;
	}

	
	public final void setVisible(boolean isVisible)
	{
		if (m_parent != null && isVisible)
			m_parent.setVisible(isVisible);

		m_isVisible = isVisible;
	}

	
	protected void onStyleChanged() { }

	
	protected void onEnter() { }

	
	protected void onLeave() { }

	
	public abstract void onMouseEvent(InputManager.InputMouseEvent mouseEvent);

	
	public abstract void onKeyEvent(InputManager.InputKeyEvent keyEvent);

	
	public abstract Rectangle getBounds();

	
	public abstract void update(int deltaTime);
}
