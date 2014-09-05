package io.github.jevaengine.graphics;

import io.github.jevaengine.IAssetStreamFactory;
import io.github.jevaengine.IAssetStreamFactory.AssetStreamConstructionException;
import io.github.jevaengine.game.IRenderer;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.ThreadSafe;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class CachedBufferedGraphicFactory implements IGraphicFactory
{
	private final IRenderer m_renderer;
	private final IAssetStreamFactory m_assetFactory;
	
	private final HashMap<String, WeakReference<BufferedImage>> m_imageCache = new HashMap<String, WeakReference<BufferedImage>>();
	
	@Inject
	public CachedBufferedGraphicFactory(IRenderer renderer, IAssetStreamFactory assetFactory)
	{
		m_renderer = renderer;
		m_assetFactory = assetFactory;
	}
	
	@Nullable
	private static BufferedImage createCompatibleImage(GraphicsConfiguration graphicsConfiguration, InputStream is) throws IOException
	{
		Image srcImage = ImageIO.read(is);
		
		if(srcImage == null)
			return null;
		
		BufferedImage destImage = graphicsConfiguration.createCompatibleImage(srcImage.getWidth(null), srcImage.getHeight(null), Transparency.TRANSLUCENT);
		Graphics g = destImage.getGraphics();
		g.drawImage(srcImage, 0, 0, null);
		g.dispose();
		return destImage;
	}
	
	@Override
	@ThreadSafe
	public IImmutableGraphic create(String name) throws GraphicConstructionException
	{
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
					img = createCompatibleImage(m_renderer.getGraphicsConfiguration(), m_assetFactory.create(formal));
					
					if(img == null)
						throw new GraphicConstructionException(formal, new UnsupportedGraphicFormatException());
					
					m_imageCache.put(formal, new WeakReference<BufferedImage>(img));
				}catch(AssetStreamConstructionException | IOException e) {
					throw new GraphicConstructionException(formal, e);
				}
			}
			
			return new BufferedGraphic(img);
		}
	}

	@Override
	public IGraphic create(int width, int height)
	{
		return new BufferedGraphic(new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR));
	}
}
