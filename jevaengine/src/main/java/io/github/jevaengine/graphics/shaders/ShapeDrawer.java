package io.github.jevaengine.graphics.shaders;

import java.awt.Shape;

import org.jogamp.glg2d.PathVisitor;
import org.jogamp.glg2d.impl.gl2.GL2ShapeDrawer;

public class ShapeDrawer extends GL2ShapeDrawer 
{
	private PrimitiveShader m_shader;
	
	public ShapeDrawer(PrimitiveShader shader)
	{
		m_shader = shader;
	}
	
	@Override
	protected void traceShape(Shape shape, PathVisitor visitor)
	{
		m_shader.setShaderConfiguration(new PrimitiveShader.PrimitiveColour(m_shader.getWorkingColor()));
		
		super.traceShape(shape, visitor);
	}
}
