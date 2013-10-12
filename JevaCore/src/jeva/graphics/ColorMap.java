package jeva.graphics;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

public final class ColorMap
{

	/** The m_pixel grabber. */
	private PixelGrabber m_pixelGrabber;

	/** The m_image. */
	private Image m_image;

	/** The m_pixel data. */
	private int[] m_pixelData;

	/** The m_width. */
	private int m_width;

	/** The m_height. */
	private int m_height;

	/**
	 * Instantiates a new color map.
	 * 
	 * @param source
	 *            the source
	 */
	public ColorMap(InputStream source)
	{
		try
		{
			m_image = ImageIO.read(source);
		} catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}

		m_height = m_image.getHeight(null);
		m_width = m_image.getWidth(null);

		m_pixelGrabber = new PixelGrabber(m_image, 0, 0, -1, -1, true);
		try
		{
			m_pixelGrabber.grabPixels();
		} catch (InterruptedException e)
		{
		}

		m_pixelData = (int[]) m_pixelGrabber.getPixels();
	}

	/**
	 * Gets the rgb color.
	 * 
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @return the rgb color
	 */
	public int getRgbColor(int x, int y)
	{
		int colorIndex = x + (y * m_width);
		Color colBuffer = new Color(m_pixelData[colorIndex], true);

		return colBuffer.getRGB();
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
	 * Gets the height.
	 * 
	 * @return the height
	 */
	public int getHeight()
	{
		return m_height;
	}
}
