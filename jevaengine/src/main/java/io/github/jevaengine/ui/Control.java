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
package io.github.jevaengine.ui;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.style.ComponentStyle;
import io.github.jevaengine.ui.style.IUIStyle;
import io.github.jevaengine.ui.style.NullUIStyle;
import io.github.jevaengine.util.Nullable;

public abstract class Control implements IImmutableControl, IDisposable
{
	private final String m_componentName;
	
	private final String m_instanceName;
	
	private Control m_parent;
	private IUIStyle m_style = new NullUIStyle();
	
	private Vector2D m_location = new Vector2D();

	private boolean m_isVisible = true;
	private boolean m_hasFocus = false;
	
	public Control(String componentName)
	{
		this(componentName, null);
	}
	
	public Control(String componentName, @Nullable String instanceName)
	{
		m_componentName = componentName;
		m_instanceName = instanceName == null ? "__CONTROL_UNAMED" : instanceName;
	}
	
	@Override
	public void dispose()
	{
		if(m_style != null)
			m_style.dispose();
	}

	public final boolean hasFocus()
	{
		return m_hasFocus;
	}
	
	final void setFocus()
	{
		m_hasFocus = true;
		onFocusChanged();
	}
	
	final void clearFocus()
	{
		m_hasFocus = false;
		onFocusChanged();
	}
	
	@Override
	public final String getInstanceName()
	{
		return m_instanceName;
	}
	
	public final Vector2D getLocation()
	{
		return new Vector2D(m_location);
	}

	public final Vector2D getAbsoluteLocation()
	{
		if (m_parent != null)
			return m_location.add(m_parent.getAbsoluteLocation());
		else
			return getLocation();
	}
	public final void setLocation(Vector2D location)
	{
		m_location = new Vector2D(location);
	}

	public final ComponentStyle getComponentStyle()
	{
		return m_style.getComponentStyle(m_componentName);
	}

	public final IUIStyle getStyle()
	{
		return m_style;
	}
	
	public final void setStyle(IUIStyle style)
	{
		if(m_style != null)
			m_style.dispose();
		
		m_style = style;
		onStyleChanged();
	}

	public final void setParent(Control parent)
	{
		m_parent = parent;

		if (m_parent != null)
			setStyle(m_parent.m_style);
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

	protected void onStyleChanged()
	{
	}

	protected void onEnter()
	{
	}

	protected void onLeave()
	{
	}
	
	protected void onFocusChanged()
	{
	}
}
