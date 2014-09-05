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
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.math.Rect2D;

import java.awt.Color;
import java.awt.Graphics2D;

public class ValueGuage extends Control implements IRenderable
{
	public static final String COMPONENT_NAME = "valueGuage";
	
	private Color m_color;
	private int m_width;
	private int m_height;
	private float m_value;

	private IImmutableGraphic m_frame;
	
	public ValueGuage(Color color, int width, int height)
	{
		super(COMPONENT_NAME);
		m_color = color;
		m_width = width;
		m_height = height;
		m_frame = new NullGraphic(width, height);
	}
	
	public ValueGuage(String instanceName, Color color, int width, int height)
	{
		super(COMPONENT_NAME, instanceName);
		m_color = color;
		m_width = width;
		m_height = height;
	}

	public void setValue(float value)
	{
		m_value = value;
	}

	public float getValue()
	{
		return m_value;
	}

	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		m_frame.render(g, x, y, scale);
		g.setColor(m_color);
		g.fillRect(x + 2, y + 2, (int) ((m_width - 2) * m_value), m_height - 4);
	}

	@Override
	public Rect2D getBounds()
	{
		return new Rect2D(0, 0, m_width, m_height);
	}

	@Override
	public boolean onMouseEvent(InputMouseEvent mouseEvent) { return false; }

	@Override
	public boolean onKeyEvent(InputKeyEvent keyEvent) { return false; }

	@Override
	public void update(int deltaTime) { }

	@Override
	protected void onStyleChanged()
	{
		m_frame = getComponentStyle().getStateStyle(ComponentState.Default).createFrame(m_width, m_height);
	}
	
}
