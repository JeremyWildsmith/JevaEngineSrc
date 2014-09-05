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
package io.github.jevaengine.ui;

import io.github.jevaengine.graphics.IImmutableGraphic;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.NullGraphic;
import io.github.jevaengine.graphics.NullRenderable;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.math.Rect2D;

import java.awt.Graphics2D;

public final class Viewport extends Control
{
	public static final String COMPONENT_NAME = "viewport";
	
	private final int m_desiredWidth;
	private final int m_desiredHeight;
	
	private IRenderable m_view;
	private IImmutableGraphic m_frame;
		
	public Viewport(int desiredWidth, int desiredHeight)
	{
		super(COMPONENT_NAME);
		m_view = new NullRenderable();
		m_frame = new NullGraphic(desiredWidth, desiredHeight);
		m_desiredWidth = desiredWidth;
		m_desiredHeight = desiredHeight;
	}
	
	public Viewport(String instanceName, int desiredWidth, int desiredHeight)
	{
		super(COMPONENT_NAME, instanceName);
		m_view = new NullRenderable();
		m_frame = new NullGraphic(desiredWidth, desiredHeight);
		m_desiredWidth = desiredWidth;
		m_desiredHeight = desiredHeight;	
	}
	
	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		m_frame.render(g, x, y, scale);
		m_view.render(g, x, y, scale);
	}

	public void setView(IRenderable view)
	{
		m_view = view;
	}
	
	@Override
	public Rect2D getBounds()
	{
		return m_frame.getBounds();
	}

	@Override
	public void onStyleChanged()
	{
		m_frame = getComponentStyle().getStateStyle(ComponentState.Default).createFrame(m_desiredWidth, m_desiredHeight);
	}
	
	@Override
	public boolean onMouseEvent(InputMouseEvent mouseEvent) { return false; }

	@Override
	public boolean onKeyEvent(InputKeyEvent keyEvent) { return false; }

	@Override
	public void update(int deltaTime) { }

}
