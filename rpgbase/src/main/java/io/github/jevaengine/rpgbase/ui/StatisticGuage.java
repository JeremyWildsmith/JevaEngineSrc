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
package io.github.jevaengine.rpgbase.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.ui.Control;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.math.Vector2D;

public class StatisticGuage extends Control implements IRenderable
{

	private Vector2D m_anchor;
	private Color m_color;
	private int m_width;
	private int m_height;
	private float m_fValue;
	private boolean m_isVisible;

	public StatisticGuage(Vector2D anchor, Color color, int width, int height, float fValue)
	{
		m_anchor = anchor;
		m_color = color;
		m_width = width;
		m_height = height;
		m_fValue = fValue;
		m_isVisible = true;
	}

	public void setValue(float fValue)
	{
		m_fValue = fValue;
	}

	public float getValue()
	{
		return m_fValue;
	}

	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		if (m_isVisible)
		{
			g.setColor(m_color);
			g.fillRect(x - m_anchor.x, y - m_anchor.y, (int) (m_width * m_fValue), m_height);

			g.setColor(Color.black);
			g.drawRect(x - m_anchor.x, y - m_anchor.y, m_width, m_height);
		}
	}

	@Override
	public Rectangle getBounds()
	{
		return new Rectangle(0, 0, m_width, m_height);
	}

	@Override
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{
	}

	@Override
	public void onKeyEvent(InputKeyEvent keyEvent)
	{

	}

	@Override
	public void update(int deltaTime)
	{

	}

}
