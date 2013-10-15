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

import java.awt.Image;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.imageio.ImageIO;

import jeva.Core;
import jeva.IResourceLibrary;


public final class Graphic
{

	/** The Constant m_imageCache. */
	private static final HashMap<String, WeakReference<Image>> m_imageCache = new HashMap<String, WeakReference<Image>>();

	/** The m_source image. */
	private Image m_sourceImage;

	
	private Graphic(Image sourceImage)
	{
		m_sourceImage = sourceImage;
	}

	public static Graphic create(String name) throws IOException
	{
		String formal = name.replace("\\", "/").trim().toLowerCase();

		if (formal.startsWith("/"))
			formal = formal.substring(1);

		synchronized (m_imageCache)
		{
			Image img = (m_imageCache.containsKey(formal) ? m_imageCache.get(formal).get() : null);

			if (img == null)
			{
				img = ImageIO.read(Core.getService(IResourceLibrary.class).openResourceStream(formal));
				m_imageCache.put(formal, new WeakReference<Image>(img));
			}

			return new Graphic(img);
		}
	}

	
	protected Image getImage()
	{
		return m_sourceImage;
	}
}
