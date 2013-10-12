package jeva.graphics;

import java.awt.Image;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.imageio.ImageIO;

import jeva.Core;
import jeva.IResourceLibrary;

/**
 * The Class Graphic.
 */
public final class Graphic
{

	/** The Constant m_imageCache. */
	private static final HashMap<String, WeakReference<Image>> m_imageCache = new HashMap<String, WeakReference<Image>>();

	/** The m_source image. */
	private Image m_sourceImage;

	/**
	 * Instantiates a new graphic.
	 * 
	 * @param sourceImage
	 *            the source image
	 */
	private Graphic(Image sourceImage)
	{
		m_sourceImage = sourceImage;
	}

	/**
	 * Creates the.
	 * 
	 * @param name
	 *            the name
	 * @return the graphic
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
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

	/**
	 * Gets the image.
	 * 
	 * @return the image
	 */
	protected Image getImage()
	{
		return m_sourceImage;
	}
}
