package io.github.jevaengine.graphics;

import java.awt.Graphics2D;

public final class NullRenderable implements IRenderable
{
	@Override
	public void render(Graphics2D g, int x, int y, float scale) { }
}
