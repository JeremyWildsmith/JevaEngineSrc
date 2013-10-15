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

	
	public int getRgbColor(int x, int y)
	{
		int colorIndex = x + (y * m_width);
		Color colBuffer = new Color(m_pixelData[colorIndex], true);

		return colBuffer.getRGB();
	}

	
	public int getWidth()
	{
		return m_width;
	}

	
	public int getHeight()
	{
		return m_height;
	}
}
