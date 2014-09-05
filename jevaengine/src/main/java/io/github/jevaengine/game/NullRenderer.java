package io.github.jevaengine.game;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.math.Vector2D;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;

public final class NullRenderer implements IRenderer
{
	@Override
	public GraphicsConfiguration getGraphicsConfiguration()
	{
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	}

	@Override
	public void render(IRenderable frame) { }

	@Override
	public Vector2D getResolution()
	{
		return new Vector2D();
	}

}
