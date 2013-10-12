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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import jeva.graphics.Font;
import jeva.joystick.InputManager;

public class Label extends Control
{

	/** The m_text. */
	private String m_text;

	/** The m_color. */
	private Color m_color;

	/**
	 * Instantiates a new label.
	 * 
	 * @param text
	 *            the text
	 * @param color
	 *            the color
	 */
	public Label(String text, Color color)
	{
		m_color = color;
		m_text = text;
	}

	/**
	 * Gets the text.
	 * 
	 * @return the text
	 */
	public String getText()
	{
		return m_text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#getBounds()
	 */
	@Override
	public Rectangle getBounds()
	{
		Font font = getStyle().getFont(m_color);

		int width = 0;

		Rectangle[] widths = font.getString(m_text);

		for (Rectangle rect : widths)
			width += rect.width;

		return new Rectangle(0, 0, width, font.getHeight());
	}

	/**
	 * Sets the color.
	 * 
	 * @param color
	 *            the new color
	 */
	public void setColor(Color color)
	{
		m_color = color;
	}

	/**
	 * Sets the text.
	 * 
	 * @param text
	 *            the new text
	 */
	public void setText(String text)
	{
		m_text = text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#onMouseEvent(jeva.joystick.InputManager.
	 * InputMouseEvent)
	 */
	public void onMouseEvent(InputManager.InputMouseEvent mouseEvent)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.graphics.ui.Control#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	public void onKeyEvent(InputManager.InputKeyEvent keyEvent)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.IRenderable#render(java.awt.Graphics2D, int, int,
	 * float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		Font font = getStyle().getFont(m_color);

		Rectangle[] str = font.getString(m_text);

		int xOffset = x;

		for (int i = 0; i < str.length; i++)
		{
			g.drawImage(font.getSource(), xOffset, y, xOffset + str[i].width, y + str[i].height, str[i].x, str[i].y, str[i].x + str[i].width, str[i].y + str[i].height, null);
			xOffset += str[i].width;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#update(int)
	 */
	@Override
	public void update(int deltaTime)
	{

	}
}
