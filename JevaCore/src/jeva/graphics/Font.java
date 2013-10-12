package jeva.graphics;

import java.awt.Color;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.game.ResourceLoadingException;

import com.sun.istack.internal.Nullable;

/**
 * The Class Font.
 */
public final class Font
{
	/** The m_src image. */
	private Image m_srcImage;

	/** The m_character map. */
	private HashMap<Character, Rectangle> m_characterMap;

	/** The m_width. */
	private int m_width;

	/** The m_height. */
	private int m_height;

	/**
	 * Instantiates a new font.
	 * 
	 * @param srcImage
	 *            the src image
	 * @param characterMap
	 *            the character map
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	protected Font(Image srcImage, HashMap<Character, Rectangle> characterMap, int width, int height)
	{
		m_srcImage = srcImage;
		m_characterMap = characterMap;

		m_width = width;
		m_height = height;
	}

	/**
	 * Creates the.
	 * 
	 * @param root
	 *            the root
	 * @param color
	 *            the color
	 * @return the font
	 */
	public static Font create(Variable root, Color color)
	{
		Variable source = root.getVariable("source");

		int maxWidth = 0;
		int maxHeight = 0;
		try
		{
			Image srcImage = ImageIO.read(Core.getService(IResourceLibrary.class).openResourceStream(source.getValue().getString()));

			srcImage = filterImage(srcImage, color);

			HashMap<Character, Rectangle> charMap = new HashMap<Character, Rectangle>();

			for (Variable charVar : root.getVariable("char"))
			{
				Rectangle src = charVar.getValue().getRectangle();
				maxWidth = Math.max(maxWidth, src.width);
				maxHeight = Math.max(maxHeight, src.height);

				charMap.put(new Character((char) Integer.parseInt(charVar.getName())), src);
			}
			return new Font(srcImage, charMap, maxWidth, maxHeight);
		} catch (IOException e)
		{
			throw new ResourceLoadingException(source.getValue().getString());
		}
	}

	/**
	 * Filter image.
	 * 
	 * @param src
	 *            the src
	 * @param color
	 *            the color
	 * @return the image
	 */
	private static Image filterImage(Image src, final Color color)
	{
		RGBImageFilter colorFilter = new RGBImageFilter()
		{

			@Override
			public int filterRGB(int x, int y, int rgb)
			{
				if ((rgb & 0xFF000000) != 0)
				{
					if ((rgb & 0x00FF0000) >> 16 == (rgb & 0x0000FF00) >> 8 && (rgb & 0x0000FF00) >> 8 == (rgb & 0x000000FF))
					{
						float fScale = ((float) (rgb & 0x000000FF)) / (float) 0xFF;

						rgb = (rgb & 0xFF000000) | ((int) (color.getRed() * fScale)) << 16 | ((int) (color.getGreen() * fScale) << 8) | ((int) (color.getBlue() * fScale));
					}
				}
				return rgb;
			}
		};

		return Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(src.getSource(), colorFilter));
	}

	/**
	 * Gets the source.
	 * 
	 * @return the source
	 */
	public Image getSource()
	{
		return m_srcImage;
	}

	/**
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public int getHeight()
	{
		return m_height;
	}

	/**
	 * Gets the width.
	 * 
	 * @return the width
	 */
	public int getWidth()
	{
		return m_width;
	}

	/**
	 * Gets the char.
	 * 
	 * @param c
	 *            the c
	 * @return the char
	 */
	@Nullable
	public Rectangle getChar(char c)
	{
		if (!m_characterMap.containsKey(c))
			return null;

		return m_characterMap.get(c);
	}

	/**
	 * Mapping exists.
	 * 
	 * @param keyChar
	 *            the key char
	 * @return true, if successful
	 */
	public boolean mappingExists(char keyChar)
	{
		return getChar(keyChar) != null;
	}

	/**
	 * Gets the string.
	 * 
	 * @param text
	 *            the text
	 * @return the string
	 */
	public Rectangle[] getString(String text)
	{
		ArrayList<Rectangle> stringRects = new ArrayList<Rectangle>();

		for (int i = 0; i < text.length(); i++)
		{
			Rectangle charRect = getChar(text.charAt(i));

			if (charRect != null)
				stringRects.add(charRect);
		}

		return stringRects.toArray(new Rectangle[stringRects.size()]);
	}

}
