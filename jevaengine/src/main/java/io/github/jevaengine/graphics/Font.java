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

import java.awt.Color;
import java.awt.image.RGBImageFilter;
import java.util.ArrayList;
import java.util.HashMap;

import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.Font.FontDeclaration.GlyphDeclaration;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.util.Nullable;

public final class Font
{
	private Graphic m_srcImage;

	private HashMap<Character, Rect2D> m_characterMap;

	private int m_width;

	private int m_height;

	protected Font(Graphic srcImage, HashMap<Character, Rect2D> characterMap, int width, int height)
	{
		m_srcImage = srcImage;
		m_characterMap = characterMap;

		m_width = width;
		m_height = height;
	}

	public static Font create(IImmutableVariable root, Color color)
	{
		FontDeclaration fontDecl = root.getValue(FontDeclaration.class);

		int maxWidth = 0;
		int maxHeight = 0;
		
		Graphic srcImage = Graphic.create(fontDecl.texture);
			
		srcImage = filterImage(srcImage, color);

		HashMap<Character, Rect2D> charMap = new HashMap<Character, Rect2D>();

		for (GlyphDeclaration glyph : fontDecl.glyphs)
		{
			maxWidth = Math.max(maxWidth, glyph.region.width);
			maxHeight = Math.max(maxHeight, glyph.region.height);

			charMap.put(glyph.character, glyph.region);
		}
		
		return new Font(srcImage, charMap, maxWidth, maxHeight);
	}

	private static Graphic filterImage(Graphic src, final Color color)
	{
		return src.filterImage(new RGBImageFilter()
		{

			@Override
			public int filterRGB(int x, int y, int rgb)
			{
				if ((rgb & 0xFF000000) != 0)
				{
					if ((rgb & 0x00FF0000) >> 16 == (rgb & 0x0000FF00) >> 8 && (rgb & 0x0000FF00) >> 8 == (rgb & 0x000000FF))
					{
						float scale = ((float) (rgb & 0x000000FF)) / (float) 0xFF;

						rgb = (rgb & 0xFF000000) | ((int) (color.getRed() * scale)) << 16 | ((int) (color.getGreen() * scale) << 8) | ((int) (color.getBlue() * scale));
					}
				}
				return rgb;
			}
		});
	}

	public Graphic getSource()
	{
		return m_srcImage;
	}

	public int getHeight()
	{
		return m_height;
	}

	public int getWidth()
	{
		return m_width;
	}

	@Nullable
	public Rect2D getChar(char c)
	{
		if (!m_characterMap.containsKey(c))
			return null;

		return m_characterMap.get(c);
	}

	public boolean mappingExists(char keyChar)
	{
		return getChar(keyChar) != null;
	}

	public Rect2D[] getString(String text)
	{
		ArrayList<Rect2D> stringRects = new ArrayList<Rect2D>();

		for (int i = 0; i < text.length(); i++)
		{
			Rect2D charRect = getChar(text.charAt(i));

			if (charRect != null)
				stringRects.add(charRect);
		}

		return stringRects.toArray(new Rect2D[stringRects.size()]);
	}
	
	public static class FontDeclaration implements ISerializable
	{
		public String texture;
		public GlyphDeclaration[] glyphs;
		
		public FontDeclaration() { }

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("texture").setValue(texture);
			target.addChild("glyphs").setValue(glyphs);
		}

		@Override
		public void deserialize(IImmutableVariable source)
		{
			texture = source.getChild("texture").getValue(String.class);
			glyphs = source.getChild("glyphs").getValues(GlyphDeclaration[].class);
		}
		
		public static class GlyphDeclaration implements ISerializable
		{
			public char character;
			public Rect2D region;

			public GlyphDeclaration() { }
			
			@Override
			public void serialize(IVariable target)
			{
				target.addChild("char").setValue((int)this.character);
				target.addChild("region").setValue(this.region);
			}

			@Override
			public void deserialize(IImmutableVariable source)
			{
				this.character = (char)source.getChild("char").getValue(Integer.class).intValue();
				this.region = source.getChild("region").getValue(Rect2D.class);
			}
		}
	}

}
