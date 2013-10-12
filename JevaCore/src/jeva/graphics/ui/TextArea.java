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
import java.util.ArrayList;
import java.util.Arrays;

import jeva.graphics.Font;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.joystick.InputManager.InputMouseEvent.EventType;

/**
 * The Class TextArea.
 */
public class TextArea extends Panel
{

	/** The Constant TYPE_LENGTH. */
	private static final int TYPE_LENGTH = 25;

	/**
	 * The Enum DisplayEffect.
	 */
	public enum DisplayEffect
	{

		/** The Typewriter. */
		Typewriter,
		/** The None. */
		None
	}

	/** The m_text. */
	private String m_text;

	/** The m_render text. */
	private String m_renderText;

	/** The m_font. */
	private Font m_font;

	/** The m_f scroll. */
	private float m_fScroll;

	/** The m_color. */
	private Color m_color;

	/** The m_elapsed time. */
	private int m_elapsedTime;

	/** The m_display effect. */
	private DisplayEffect m_displayEffect;

	/** The m_allow edit. */
	private boolean m_allowEdit;

	/**
	 * Instantiates a new text area.
	 * 
	 * @param text
	 *            the text
	 * @param color
	 *            the color
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public TextArea(String text, Color color, int width, int height)
	{
		super(width, height);
		m_allowEdit = false;
		m_color = color;
		m_text = text;
		m_renderText = m_text;
		m_displayEffect = DisplayEffect.None;
	}

	/**
	 * Instantiates a new text area.
	 * 
	 * @param color
	 *            the color
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public TextArea(Color color, int width, int height)
	{
		this("", color, width, height);
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

	/**
	 * Sets the effect.
	 * 
	 * @param effect
	 *            the new effect
	 */
	public void setEffect(DisplayEffect effect)
	{
		m_displayEffect = effect;

		if (effect == DisplayEffect.None)
			m_renderText = m_text;
		else if (effect == DisplayEffect.Typewriter)
			m_renderText = "";
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
		m_fScroll = 0;
		setEffect(m_displayEffect);
	}

	/**
	 * Append text.
	 * 
	 * @param text
	 *            the text
	 */
	public void appendText(String text)
	{
		m_text = getText() + text;
		setEffect(m_displayEffect);
	}

	/**
	 * Sets the editable.
	 * 
	 * @param isEditable
	 *            the new editable
	 */
	public void setEditable(boolean isEditable)
	{
		m_allowEdit = isEditable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#render(java.awt.Graphics2D, int, int, float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		super.render(g, x, y, fScale);

		String[] text = m_renderText.split("(?<=[ \n])");

		ArrayList<ArrayList<Rectangle>> lines = new ArrayList<ArrayList<Rectangle>>();
		lines.add(new ArrayList<Rectangle>());

		int offsetX = 0;

		for (String s : text)
		{
			Rectangle[] strMap = m_font.getString(s);

			int wordWidth = 0;

			for (Rectangle r : strMap)
				wordWidth += r.width;

			if (offsetX + wordWidth >= this.getBounds().width && offsetX != 0)
			{
				lines.add(new ArrayList<Rectangle>());
				offsetX = 0;
			}

			lines.get(lines.size() - 1).addAll(Arrays.asList(strMap));
			offsetX += wordWidth;

			if (s.endsWith("\n"))
			{
				lines.add(new ArrayList<Rectangle>());
				offsetX = 0;
			}
		}

		int offsetY = 0;

		int minScroll = Math.max(0, lines.size() - 1 - getBounds().height / (m_font.getHeight() + 5));

		m_fScroll = Math.min(minScroll, Math.max(m_fScroll, 0));

		for (ArrayList<Rectangle> line : lines.subList((int) m_fScroll, lines.size()))
		{
			offsetX = 0;
			for (Rectangle lineChar : line)
			{
				if (offsetY < this.getBounds().height - m_font.getHeight())
					g.drawImage(m_font.getSource(), x + offsetX, y + offsetY, x + offsetX + lineChar.width, y + offsetY + lineChar.height, lineChar.x, lineChar.y, lineChar.x + lineChar.width, lineChar.y + lineChar.height, null);

				offsetX += lineChar.width;
			}

			offsetY += m_font.getHeight() + 5;
		}
	}

	/**
	 * Scroll to end.
	 */
	public void scrollToEnd()
	{
		m_fScroll = Float.MAX_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#onStyleChanged()
	 */
	@Override
	public void onStyleChanged()
	{
		super.onStyleChanged();

		if (getStyle() != null)
			m_font = getStyle().getFont(m_color);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#onMouseEvent(jeva.joystick.InputManager.
	 * InputMouseEvent)
	 */
	@Override
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{
		if (mouseEvent.type == EventType.MouseWheelMoved)
		{
			m_fScroll = Math.max(0.0F, m_fScroll + Math.signum(mouseEvent.deltaMouseWheel));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.graphics.ui.Panel#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent keyEvent)
	{
		if (!m_allowEdit || keyEvent.isConsumed)
			return;

		if (keyEvent.type == InputKeyEvent.EventType.KeyTyped)
		{
			if (keyEvent.keyChar == '\b')
			{
				keyEvent.isConsumed = true;
				setText(m_text.substring(0, Math.max(0, m_text.length() - 1)));
			} else if (keyEvent.keyChar == '\n')
			{
				keyEvent.isConsumed = true;
				setText(m_text + keyEvent.keyChar);
			} else if (m_font.mappingExists(keyEvent.keyChar))
			{
				keyEvent.isConsumed = true;
				setText(m_text + keyEvent.keyChar);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#update(int)
	 */
	@Override
	public void update(int deltaTime)
	{
		if (m_displayEffect == DisplayEffect.Typewriter)
		{
			m_elapsedTime += deltaTime;
			for (; m_elapsedTime > TYPE_LENGTH && m_renderText.length() < m_text.length(); m_elapsedTime -= TYPE_LENGTH)
			{
				m_renderText += m_text.charAt(m_renderText.length());
			}
		}
	}
}
