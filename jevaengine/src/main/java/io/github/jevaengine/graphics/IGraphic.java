package io.github.jevaengine.graphics;

import java.awt.Graphics2D;

public interface IGraphic extends IImmutableGraphic
{
	public Graphics2D createGraphics();
}
