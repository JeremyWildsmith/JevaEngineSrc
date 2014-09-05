package io.github.jevaengine.game;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.math.Vector2D;

import java.awt.GraphicsConfiguration;

public interface IRenderer
{
	GraphicsConfiguration getGraphicsConfiguration();
	void render(IRenderable frame);
	Vector2D getResolution();
}
