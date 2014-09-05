package io.github.jevaengine.graphics;

import io.github.jevaengine.math.Rect2D;

import java.awt.Graphics2D;
import java.awt.image.RGBImageFilter;

public interface IImmutableGraphic extends IRenderable
{
	void render(Graphics2D g, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh);
	
	@Override
	void render(Graphics2D g, int dx, int dy, float scale);
	
	IImmutableGraphic filterImage(RGBImageFilter filter);
	boolean pickTest(int x, int y);
	Rect2D getBounds();
}
