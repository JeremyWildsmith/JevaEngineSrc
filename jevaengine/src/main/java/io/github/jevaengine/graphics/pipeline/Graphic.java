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
package io.github.jevaengine.graphics.pipeline;

import io.github.jevaengine.CoreModeViolationException;
import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceFormatException;
import io.github.jevaengine.ResourceIOException;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.util.Nullable;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.imageio.ImageIO;

public final class Graphic
{
	private static final HashMap<String, WeakReference<BufferedImage>> m_imageCache = new HashMap<String, WeakReference<BufferedImage>>();

	private @Nullable BufferedImage m_sourceImage;

	private Graphic(@Nullable BufferedImage sourceImage)
	{
		m_sourceImage = sourceImage;
	}
	
	public static Graphic create(String name)
	{
		if(!Core.getMode().allowsRender())
			return new Graphic(null);
		
		String formal = name.replace("\\", "/").trim();

		if (formal.startsWith("/"))
			formal = formal.substring(1);
		
		synchronized (m_imageCache)
		{
			BufferedImage img = (m_imageCache.containsKey(formal) ? m_imageCache.get(formal).get() : null);
			
			if (img == null)
			{
				try
				{
					img = ImageIO.read(Core.getService(ResourceLibrary.class).openAsset(formal));
					
					if(img == null)
						throw new ResourceFormatException("Unknown image format", formal);
					
					m_imageCache.put(formal, new WeakReference<BufferedImage>(img));
				}catch(IOException e) {
					throw new ResourceIOException(e, name);
				}
			}
			
			return new Graphic(img);
		}
	}
	
	Image get()
	{
		if(m_sourceImage == null)
			throw new CoreModeViolationException("Core mode does not permit rendering or graphics operations.");
		
		return m_sourceImage;
	}
	
	public void render(Graphics2D g, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh)
	{
		if(m_sourceImage == null)
			throw new CoreModeViolationException("Core mode does not permit rendering or graphics operations.");
		
		g.drawImage(m_sourceImage, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
	}
	
	public void render(Graphics2D g, int dx, int dy, float scale)
	{
		if(m_sourceImage == null)
			throw new CoreModeViolationException("Core mode does not permit rendering or graphics operations.");
		
		render(g, dx, dy, (int)(m_sourceImage.getWidth() * scale), (int)(m_sourceImage.getHeight() * scale),
					0, 0, m_sourceImage.getWidth(), m_sourceImage.getHeight());
	}
	
	public Graphic filterImage(RGBImageFilter filter)
	{
		if(m_sourceImage == null)
			return new Graphic(null);
		else
		{
			Image srcImage = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(m_sourceImage.getSource(), filter));
			BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(null), srcImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = bufferedImage.createGraphics();
			g.drawImage(srcImage, 0, 0, null);
			g.dispose();
			
			return new Graphic(bufferedImage);
		}
	}
	
	public boolean pickTest(int x, int y)
	{
		if(x < 0 || y < 0 || x >= m_sourceImage.getWidth(null) || y >= m_sourceImage.getHeight(null))
			return false;
		else
			return ((m_sourceImage.getRGB(x, y) >> 24) & 0xff) != 0;
	}
}
