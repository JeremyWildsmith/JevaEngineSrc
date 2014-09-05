package io.github.jevaengine.ui.style;

import io.github.jevaengine.graphics.IGraphic;
import io.github.jevaengine.graphics.IGraphicFactory;
import io.github.jevaengine.graphics.IImmutableGraphic;
import io.github.jevaengine.graphics.Sprite;

import java.awt.Graphics2D;

public final class TiledFrameFactory implements IFrameFactory
{
	private IGraphicFactory m_graphicFactory;
	
	private Sprite m_topLeft;
	private Sprite m_top;
	private Sprite m_topRight;
	private Sprite m_left;
	private Sprite m_fill;
	private Sprite m_right;
	private Sprite m_bottomLeft;
	private Sprite m_bottom;
	private Sprite m_bottomRight;
	
	public TiledFrameFactory(IGraphicFactory graphicFactory, Sprite topLeft, Sprite top, Sprite topRight, Sprite left, Sprite fill, Sprite right, Sprite bottomLeft, Sprite bottom, Sprite bottomRight)
	{
		m_graphicFactory = graphicFactory;
		m_topLeft = topLeft;
		m_top = top;
		m_topRight = topRight;
		m_left = left;
		m_fill = fill;
		m_right = right;
		m_bottomLeft = bottomLeft;
		m_bottom = bottom;
		m_bottomRight = bottomRight;
	}

	@Override
	public IImmutableGraphic create(int desiredWidth, int desiredHeight)
	{
		int minHeight = m_topLeft.getBounds().height + m_bottomLeft.getBounds().height;
		int minWidth = m_topLeft.getBounds().width + m_topRight.getBounds().width;
		
		int height = m_left.getBounds().height * (int)(Math.floor(Math.max(minHeight, desiredHeight) / m_left.getBounds().height));
		int width = m_left.getBounds().width * (int)(Math.floor(Math.max(minWidth, desiredWidth) / m_left.getBounds().width));
		
		IGraphic constructedFrame = m_graphicFactory.create(width, height);

		Graphics2D g = constructedFrame.createGraphics();

		// Render upper border
		m_topLeft.render(g, 0, 0, 1.0F);

		int offsetX;
		for (offsetX = m_topLeft.getBounds().width; offsetX < width - m_topRight.getBounds().width; offsetX += m_top.getBounds().width)
			m_top.render(g, offsetX, 0, 1.0F);

		m_topRight.render(g, offsetX, 0, 1.0F);
		offsetX += m_topRight.getBounds().width;

		int offsetY = m_top.getBounds().height;

		// Render fill and left\right border
		for (; offsetY < height - m_bottom.getBounds().height; offsetY += m_fill.getBounds().width)
		{
			m_left.render(g, 0, offsetY, 1.0F);

			for (offsetX = m_left.getBounds().width; offsetX < width - m_right.getBounds().width; offsetX += m_fill.getBounds().width)
				m_fill.render(g, offsetX, offsetY, 1.0F);

			m_right.render(g, offsetX, offsetY, 1.0F);
		}

		// Render lower border
		offsetX = 0;
		m_bottomLeft.render(g, offsetX, offsetY, 1.0F);
		
		for (offsetX = m_bottomLeft.getBounds().width; offsetX < width - m_bottomRight.getBounds().width; offsetX += m_top.getBounds().width)
			m_bottom.render(g, offsetX, offsetY, 1.0F);

		m_bottomRight.render(g, offsetX, offsetY, 1.0F);

		g.dispose();
		
		return constructedFrame;
	}
}