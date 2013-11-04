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
package io.github.jevaengine.graphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import io.github.jevaengine.math.Vector2D;

public final class Text implements IRenderable
{

	private Vector2D m_anchor;

	private Font m_font;

	private String m_text;

	private float m_fScale;

	public Text(String text, Vector2D anchor, Font font, float fScale)
	{
		m_text = text;
		m_anchor = anchor;
		m_font = font;
		m_fScale = fScale;
	}

	public void setText(String text)
	{
		m_text = text;
	}

	public String getText()
	{
		return m_text;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.IRenderable#render(java.awt.Graphics2D, int, int,
	 * float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		Rectangle[] str = m_font.getString(m_text);

		int xOffset = x;

		for (int i = 0; i < str.length; i++)
		{
			g.drawImage(m_font.getSource(), -m_anchor.x + xOffset, -m_anchor.y + y, (int) (xOffset + str[i].width * fScale * m_fScale), (int) (y + str[i].height * fScale * m_fScale), str[i].x, str[i].y, str[i].x + str[i].width, str[i].y + str[i].height, null);
			xOffset += str[i].width * fScale * m_fScale;
		}
	}
}
