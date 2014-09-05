package io.github.jevaengine.graphics;

import io.github.jevaengine.math.Rect2D;

import java.awt.Graphics2D;
import java.awt.image.RGBImageFilter;

public final class NullGraphic implements IImmutableGraphic
{
	private final int m_width;
	private final int m_height;
	
	public NullGraphic(int width, int height)
	{
		m_width = width;
		m_height = height;
	}
	
	public NullGraphic()
	{
		this(1, 1);
	}
	
	@Override
	public IImmutableGraphic filterImage(RGBImageFilter filter)
	{
		return this;
	}

	@Override
	public boolean pickTest(int x, int y)
	{
		return false;
	}

	@Override
	public Rect2D getBounds()
	{
		return new Rect2D(m_width, m_height);
	}

	@Override
	public void render(Graphics2D g, int dx, int dy, int dw, int dh, int sx, int sy, int sw, int sh) { }

	@Override
	public void render(Graphics2D g, int dx, int dy, float scale) { }
}
