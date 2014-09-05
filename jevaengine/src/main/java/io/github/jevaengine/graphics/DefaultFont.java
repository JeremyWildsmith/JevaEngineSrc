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

import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.util.Nullable;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.HashMap;

public final class DefaultFont implements IFont
{
	private final IImmutableGraphic m_srcImage;
	private final HashMap<Character, Rect2D> m_characterMap;

	private final Rect2D m_maxCharacterBounds;
	
	public DefaultFont(IImmutableGraphic srcImage, HashMap<Character, Rect2D> characterMap)
	{
		m_srcImage = srcImage;
		m_characterMap = characterMap;
		
		m_maxCharacterBounds = getMaxBounds(characterMap.values());
	}
	
	private Rect2D getMaxBounds(Collection<Rect2D> bounds)
	{
		int maxWidth = 0;
		int maxHeight = 0;
		
		for(Rect2D r : bounds)
		{
			maxWidth = Math.max(maxWidth, r.width);
			maxHeight = Math.max(maxHeight, r.height);
		}
		
		return new Rect2D(maxWidth, maxHeight);
	}

	@Nullable
	private Rect2D getChar(char c)
	{
		if (!m_characterMap.containsKey(c))
			return null;

		return m_characterMap.get(c);
	}

	@Override
	public boolean doesMappingExists(char keyChar)
	{
		return getChar(keyChar) != null;
	}
	
	@Override
	public Rect2D getMaxCharacterBounds()
	{
		return new Rect2D(m_maxCharacterBounds);
	}
	
	@Override
	public Rect2D getTextBounds(String text)
	{
		int x = 0;
		int y = 0;
		
		for(char c : text.toCharArray())
		{
			Rect2D charBounds = getChar(c);
			
			if(charBounds != null)
			{
				x += charBounds.width;
				y = Math.max(y, charBounds.height);
			}
		}
		
		return new Rect2D(x, y);
	}
	
	@Override
	public Rect2D drawText(Graphics2D g, int x, int y, float scale, String text)
	{
		float currentX = 0;
		float maxY = 0;
		
		for(char c : text.toCharArray())
		{
			Rect2D charBounds = getChar(c);
			if(charBounds != null)
			{
				m_srcImage.render(g, x + (int)currentX, y, (int)(charBounds.width * scale), (int)(charBounds.height * scale), 
						 charBounds.x, charBounds.y, charBounds.width, charBounds.height);
	
				currentX += charBounds.width * scale;
				maxY = Math.max(y, charBounds.height * scale);
			}
		}
		
		return new Rect2D(x, y, (int)currentX, (int)maxY);
	}
}
