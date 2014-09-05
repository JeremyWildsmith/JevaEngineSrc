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
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;

public final class BufferedGraphic implements IGraphic
{
	private BufferedImage m_sourceImage;

	public BufferedGraphic(@Nullable BufferedImage sourceImage)
	{
		m_sourceImage = sourceImage;
	}
	
	@Override
	public void render(Graphics2D g, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh)
	{
		g.drawImage(m_sourceImage, dx, dy, dx + dw, dy + dh, sx, sy, sx + sw, sy + sh, null);
	}
	
	@Override
	public void render(Graphics2D g, int dx, int dy, float scale)
	{	
		render(g, dx, dy, (int)(m_sourceImage.getWidth() * scale), (int)(m_sourceImage.getHeight() * scale),
					0, 0, m_sourceImage.getWidth(), m_sourceImage.getHeight());
	}
	
	@Override
	public IImmutableGraphic filterImage(RGBImageFilter filter)
	{
		if(m_sourceImage == null)
			return new BufferedGraphic(null);
		else
		{
			Image srcImage = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(m_sourceImage.getSource(), filter));
			BufferedImage bufferedImage = new BufferedImage(srcImage.getWidth(null), srcImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = bufferedImage.createGraphics();
			g.drawImage(srcImage, 0, 0, null);
			g.dispose();
			
			return new BufferedGraphic(bufferedImage);
		}
	}
	
	@Override
	public boolean pickTest(int x, int y)
	{
		if(x < 0 || y < 0 || x >= m_sourceImage.getWidth(null) || y >= m_sourceImage.getHeight(null))
			return false;
		else
			return ((m_sourceImage.getRGB(x, y) >> 24) & 0xff) != 0;
	}
	
	public Graphics2D createGraphics()
	{
		return (Graphics2D) m_sourceImage.getGraphics();
	}

	@Override
	public Rect2D getBounds()
	{
		return new Rect2D(m_sourceImage.getWidth(), m_sourceImage.getHeight());
	}
}
