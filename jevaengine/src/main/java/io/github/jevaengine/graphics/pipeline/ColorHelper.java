package io.github.jevaengine.graphics.pipeline;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Paint;
import java.awt.RenderingHints.Key;

import org.jogamp.glg2d.GLG2DColorHelper;
import org.jogamp.glg2d.GLGraphics2D;

public class ColorHelper implements GLG2DColorHelper
{
	private PrimitiveShader m_shader;
	private Color m_background = Color.black;
	
	public ColorHelper(PrimitiveShader shader)
	{
		m_shader = shader;
	}
	
	@Override
	public void setColorNoRespectComposite(Color c)
	{
		m_shader.setWorkingColor(c);
	}
	
	@Override
	public void setColor(Color c)
	{
		m_shader.setWorkingColor(c);
	}

	@Override
	public Color getColor()
	{
		return m_shader.getWorkingColor();
	}
	
	@Override
	public void setG2D(GLGraphics2D g2d) { }

	@Override
	public void setColorRespectComposite(Color c)
	{
		m_shader.setWorkingColor(c);
	}
	
	@Override
	public void setComposite(Composite comp)
	{
		if(!comp.equals(AlphaComposite.SrcOver))
			throw new UnsupportedOperationException();
	}

	@Override
	public Composite getComposite()
	{
		return AlphaComposite.SrcOver;
	}

	@Override
	public void setBackground(Color color)
	{
		m_background = color;
	}

	@Override
	public Color getBackground()
	{
		return m_background;
	}

	@Override
	public void setPaint(Paint paint)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPaintMode()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setXORMode(Color c)
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public void copyArea(int x, int y, int width, int height, int dx, int dy)
	{
		throw new UnsupportedOperationException();	
	}

	@Override
	public void push(GLGraphics2D newG2d)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void pop(GLGraphics2D parentG2d)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Paint getPaint()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void resetHints() { }

	@Override
	public void dispose() { }
	
	@Override
	public void setHint(Key key, Object value) { }
}
