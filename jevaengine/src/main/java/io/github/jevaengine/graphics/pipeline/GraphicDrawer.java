package io.github.jevaengine.graphics.pipeline;

import io.github.jevaengine.graphics.pipeline.GraphicRenderHints.GraphicMode;

import java.awt.Color;
import java.awt.RenderingHints.Key;
import java.awt.geom.AffineTransform;

import javax.media.opengl.GL2;

import org.jogamp.glg2d.GLGraphics2D;
import org.jogamp.glg2d.impl.AbstractImageHelper;

import com.jogamp.opengl.util.texture.Texture;

public final class GraphicDrawer extends AbstractImageHelper
{
	private GL2 m_glContext;
	private PrimitiveShader m_shader;
	
	private GraphicMode m_nextMode;
	
	public GraphicDrawer(PrimitiveShader shader)
	{
		m_shader = shader;
	}
	
	protected Texture getTexture(Graphic graphic)
	{
		return super.getTexture(graphic.get(), null);
	}

	@Override
	public void setG2D(GLGraphics2D g2d)
	{
		super.setG2D(g2d);

		if(m_glContext != g2d && m_glContext != null)
			m_shader.end();
		
		m_glContext = g2d.getGLContext().getGL().getGL2();
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
		PrimitiveShader.PrimitiveMode mode;
		
		if(m_nextMode == null)
			mode = new PrimitiveShader.PrimitiveTexture(texture);
		else
			mode = m_nextMode.create(this, texture, m_shader.getWorkingColor());

		m_shader.setShaderConfiguration(mode);
		
		m_nextMode = null;
	}

	@Override
	protected void end(Texture texture)
	{
		
	}

	@Override
	protected void applyTexture(Texture texture, int dx1, int dy1, int dx2, int dy2, float sx1, float sy1, float sx2, float sy2)
	{
		m_glContext.glBegin(GL2.GL_QUADS);

	    // SW
		m_glContext.glTexCoord2f(sx1, sy2);
		m_glContext.glVertex2i(dx1, dy2);
	    // SE
		m_glContext.glTexCoord2f(sx2, sy2);
		m_glContext.glVertex2i(dx2, dy2);
	    // NE
		m_glContext.glTexCoord2f(sx2, sy1);
		m_glContext.glVertex2i(dx2, dy1);
	    // NW
		m_glContext.glTexCoord2f(sx1, sy1);
		m_glContext.glVertex2i(dx1, dy1);

		m_glContext.glEnd();
	}
}