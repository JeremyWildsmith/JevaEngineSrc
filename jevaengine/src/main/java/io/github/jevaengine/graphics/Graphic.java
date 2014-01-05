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

import io.github.jevaengine.CoreModeViolationException;
import io.github.jevaengine.Core;
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
import java.util.BitSet;
import java.util.HashMap;

import javax.imageio.ImageIO;

public final class Graphic
{
	private static final HashMap<String, WeakReference<BufferedImage>> m_imageCache = new HashMap<String, WeakReference<BufferedImage>>();
	private static final HashMap<String, WeakReference<BitSet>> m_pickingCache = new HashMap<String, WeakReference<BitSet>>();

	private @Nullable Image m_sourceImage;
	private @Nullable BitSet m_picking;
	
	private Graphic(@Nullable Image sourceImage)
	{
		m_sourceImage = sourceImage;
	}

	private Graphic(@Nullable Image sourceImage, @Nullable BitSet picking)
	{
		m_sourceImage = sourceImage;
		m_picking = picking;
	}

	public static Graphic create(String name) throws IOException
	{
		return create(name, false);
	}
	
	public static Graphic create(String name, boolean enablePickTesting) throws IOException
	{
		if(!Core.getMode().allowsRender())
			return new Graphic(null);
		
		String formal = name.replace("\\", "/").trim();

		if (formal.startsWith("/"))
			formal = formal.substring(1);
		
		BufferedImage img;
		synchronized (m_imageCache)
		{
			img = (m_imageCache.containsKey(formal) ? m_imageCache.get(formal).get() : null);
			
			if (img == null)
			{
				img = ImageIO.read(Core.getService(ResourceLibrary.class).openAsset(formal));
				m_imageCache.put(formal, new WeakReference<BufferedImage>(img));
			}
			
			if(!enablePickTesting)
				return new Graphic(img);
		}
		
		synchronized(m_pickingCache)
		{
			int width = img.getWidth();
			int height = img.getHeight();
			
			
			BitSet picking = m_pickingCache.containsKey(formal) ? m_pickingCache.get(formal).get() : null;
			
			if (picking == null)
			{
				picking = new BitSet(height * width);
				
				for(int y = 0; y < height; y++)
				{
					for(int x = 0; x < width; x++)
					{
						int alpha = (img.getRGB(x, y) >> 24) & 0xff;
						
						if(alpha != 0)
							picking.set(y * width + x);
					}
				}
				
				m_pickingCache.put(formal, new WeakReference<BitSet>(picking));
			}
			return new Graphic(img, picking);
		}
	}
	
	public void render(Graphics2D g, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh)
	{
		if(m_sourceImage == null)
			throw new CoreModeViolationException("Core mode does not permit rendering or graphics operations.");
		
		g.drawImage(m_sourceImage, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
	}
	
	public Graphic filterImage(RGBImageFilter filter)
	{
		if(m_sourceImage == null)
			return new Graphic(null);
		else
			return new Graphic(Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(m_sourceImage.getSource(), filter)));
	}
	
	public boolean pickTest(int x, int y)
	{
		int index = y * m_sourceImage.getWidth(null) + x;
		
		if(m_picking == null || index < 0 || index >= m_picking.size())
			return false;
		else
			return m_picking.get(index);
	}
}
