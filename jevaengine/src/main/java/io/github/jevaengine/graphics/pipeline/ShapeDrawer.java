package io.github.jevaengine.graphics.pipeline;

import io.github.jevaengine.math.Rect2D;

import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import org.jogamp.glg2d.GLGraphics2D;
import org.jogamp.glg2d.PathVisitor;
import org.jogamp.glg2d.impl.AbstractShapeHelper;

public class ShapeDrawer extends AbstractShapeHelper 
{
	private DrawBatcher m_drawBatcher;
	private PrimitiveShader m_shader;
	
	public ShapeDrawer(PrimitiveShader shader, DrawBatcher drawBatcher)
	{
		m_shader = shader;
		m_drawBatcher = drawBatcher;
	}
	
	@Override
	public void setG2D(GLGraphics2D g2d)
	{
		super.setG2D(g2d);
	}
	
	@Override
	protected void traceShape(Shape shape, PathVisitor visitor)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void draw(Shape shape)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fill(Shape shape, boolean isDefinitelySimpleConvex)
	{
		if(shape instanceof Rectangle2D)
		{
			Rectangle2D rect = (Rectangle2D)shape;
			
			m_drawBatcher.draw(
					new PrimitiveShader.PrimitiveColour(m_shader.getWorkingColor()),
					new Rect2D((int)rect.getX(), (int)rect.getY(), (int)rect.getWidth(), (int)rect.getHeight()));
			
		}else
			throw new UnsupportedOperationException();
	}
}
