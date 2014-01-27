package io.github.jevaengine.graphics.pipeline;

import io.github.jevaengine.graphics.pipeline.GraphicRenderHints.GraphicMode;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Rect2F;

import java.awt.Color;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;

import javax.media.opengl.GL4;

import org.jogamp.glg2d.GLGraphics2D;
import org.jogamp.glg2d.impl.AbstractImageHelper;

import com.jogamp.opengl.util.texture.Texture;

public final class GraphicDrawer extends AbstractImageHelper
{
	private GL4 m_glContext;
	
	private PrimitiveShader m_shader;
	private DrawBatcher m_drawBatcher;
	
	private GraphicMode m_nextMode;

	private AffineTransform m_lastTransform;
	
	public GraphicDrawer(PrimitiveShader shader, DrawBatcher drawBatcher)
	{
		m_shader = shader;
		m_drawBatcher = drawBatcher;
	}
	
	protected Texture getTexture(Graphic graphic)
	{
		return super.getTexture(graphic.get(), null);
	}

	@Override
	public void setG2D(GLGraphics2D g2d)
	{
		super.setG2D(g2d);
		m_glContext = g2d.getGLContext().getGL().getGL4();
		m_glContext.glEnable(GL4.GL_BLEND);
		m_glContext.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	@Override
	public void setHint(Key key, Object value)
	{
		if(key == GraphicRenderHints.KEY_MODE)
			m_nextMode = (GraphicMode)value;
	}
	
	@Override
	protected void begin(Texture texture, AffineTransform xform, Color bgcolor)
	{
	}

	@Override
	protected void end(Texture texture)
	{
		if(m_lastTransform != null)
			g2d.setTransform(m_lastTransform);
		
		m_nextMode = null;
	}

	@Override
	protected void applyTexture(Texture texture, int dx1, int dy1, int dx2, int dy2, float sx1, float sy1, float sx2, float sy2)
	{
		PrimitiveShader.PrimitiveMode mode;
		
		if(m_nextMode == null)
			mode = new PrimitiveShader.PrimitiveTexture(texture);
		else
			mode = m_nextMode.create(this, texture, m_shader.getWorkingColor());
		
		m_drawBatcher.draw(mode, new Rect2D(dx1, dy1, dx2 - dx1, dy2 - dy1), new Rect2F(sx1, sy1, sx2 - sx1, sy2 - sy1));
	}
}