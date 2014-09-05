package io.github.jevaengine.ui.style;

import io.github.jevaengine.graphics.IGraphic;
import io.github.jevaengine.graphics.IGraphicFactory;
import io.github.jevaengine.graphics.IImmutableGraphic;
import io.github.jevaengine.graphics.Sprite;

import java.awt.Graphics2D;

public final class BackgroundFrameFactory implements IFrameFactory
{
	private IGraphicFactory m_graphicFactory;
	private Sprite m_background;	
	
	public BackgroundFrameFactory(IGraphicFactory graphicFactory, Sprite background)
	{
		m_graphicFactory = graphicFactory;
		m_background = background;
	}
	
	@Override
	public IImmutableGraphic create(int desiredWidth, int desiredHeight)
	{
		IGraphic background = m_graphicFactory.create(m_background.getBounds().width, m_background.getBounds().height);
		
		Graphics2D g = background.createGraphics();
		m_background.render(g, 0, 0, 1.0F);
		g.dispose();
		
		return background;
	}
	
}