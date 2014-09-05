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

import io.github.jevaengine.graphics.IFont;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.math.Rect2D;

import java.awt.Graphics2D;

public final class Label extends Control
{
	public static final String COMPONENT_NAME = "label";
	private String m_text;

	public Label(String text)
	{
		super(COMPONENT_NAME);
		m_text = text;
	}

	public Label(String instanceName, String text)
	{
		super(COMPONENT_NAME, instanceName);
		m_text = text;
	}
	
	public Label()
	{
		super(COMPONENT_NAME);
		m_text = "";
	}

	public String getText()
	{
		return m_text;
	}

	@Override
	public Rect2D getBounds()
	{
		IFont font = getComponentStyle().getStateStyle(ComponentState.Default).getFont();	
		return font.getTextBounds(m_text);
	}
	
	public void setText(String text)
	{
		m_text = text;
	}

	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		IFont font = getComponentStyle().getStateStyle(ComponentState.Default).getFont();
		font.drawText(g, x, y, scale, m_text);
	}

	public boolean onMouseEvent(InputMouseEvent mouseEvent)
	{
		return false;
	}

	public boolean onKeyEvent(InputKeyEvent keyEvent)
	{
		return false;
	}

	@Override
	public void update(int deltaTime) { }
}
