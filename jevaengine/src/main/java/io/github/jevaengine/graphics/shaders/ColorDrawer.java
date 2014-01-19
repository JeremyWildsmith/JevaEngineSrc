package io.github.jevaengine.graphics.shaders;

import java.awt.Color;

import org.jogamp.glg2d.impl.gl2.GL2ColorHelper;

public class ColorDrawer extends GL2ColorHelper
{
	private PrimitiveShader m_shader;
	
	public ColorDrawer(PrimitiveShader shader)
	{
		m_shader = shader;
	}
	
	@Override
	public void setColorRespectComposite(Color c)
	{
		setColorNoRespectComposite(c);
	}
	
	@Override
	public void setColorNoRespectComposite(Color c)
	{
		m_shader.setWorkingColor(c);
	}
}
