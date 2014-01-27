package io.github.jevaengine.graphics.pipeline;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.util.Nullable;

import java.awt.Color;

import javax.media.opengl.GL;
import javax.media.opengl.GL4;

import com.jogamp.opengl.util.texture.Texture;

public final class PrimitiveShader implements IDisposable
{
	private static final String FRAGMENT_SHADER = "primitive.f";
	private static final String VERTEX_SHADER = "primitive.v";
	
	private GraphicShader m_shader = new GraphicShader();
	
	private Color m_workingColor = Color.BLACK;
	
	@Nullable
	private PrimitiveMode m_lastMode;
	
	@Nullable
	private GL4 m_glContext;
	
	private int m_vertexPositionAttribute = -1;
	private int m_vertexTexCoordAttribute = -1;
	
	private enum ShaderMode
	{
		PrimitiveTexture(0),
		PrimitiveColor(1),
		ColorReplace(2),
		ColorMap(3);
		
		private int m_id;
		
		ShaderMode(int id)
		{
			m_id = id;
		}
		
		public int getId()
		{
			return m_id;
		}
	}
	
	public PrimitiveShader()
	{
		m_shader.loadFragmentShader(getClass().getResourceAsStream(FRAGMENT_SHADER), "UTF-8");
		m_shader.loadVertexShader(getClass().getResourceAsStream(VERTEX_SHADER), "UTF-8");
	}
	
	@Override
	public void dispose()
	{
		m_shader.dispose();
	}
	
	private void setMode(ShaderMode mode)
	{
		m_shader.setUniform1D("mode", mode.getId());
	}
	
	private void setSourceColor(Color color)
	{
		m_shader.setUniformColor("srcColor", color);
	}
	
	private void setAuxColor(Color color)
	{
		m_shader.setUniformColor("auxColor", color);
	}
	
	private void setSourceTexture(Texture texture)
	{
		m_glContext.glActiveTexture(GL.GL_TEXTURE0);
		texture.bind(m_glContext);
		m_shader.setUniform1D("srcTexture", 0);
	}
	
	private void setAuxTexture(Texture texture)
	{
		m_glContext.glActiveTexture(GL.GL_TEXTURE1);
		texture.bind(m_glContext);
		m_shader.setUniform1D("auxTexture", 1);
	}
	
	void setProjection(float[] projection)
	{
		m_shader.setUniform4F4("projectionMatrix", projection);
	}
	
	void setModelView(float[] modelView)
	{
		m_shader.setUniform4F4("modelViewMatrix", modelView);
	}
	
	void bindVertexTexCoordAttribute(int index)
	{
		if(m_vertexTexCoordAttribute == index)
			return;
		
		m_vertexTexCoordAttribute = index;
		m_shader.bindAttribute(index, "vertexTexCoord");
	}
	
	void bindVertexPositionAttribute(int index)
	{
		if(m_vertexPositionAttribute == index)
			return;
		
		m_vertexPositionAttribute = index;
		m_shader.bindAttribute(index, "vertexPosition");
	}
	
	public void setWorkingColor(Color color)
	{
		m_workingColor = color;
	}
	
	public Color getWorkingColor()
	{
		return m_workingColor;
	}
	
	public void load(GL4 g)
	{
		m_shader.load(g);
		m_glContext = g;
	}
	
	public void unload()
	{
		m_shader.unload();
		m_glContext = null;
	}
	
	public void setShaderConfiguration(PrimitiveMode mode)
	{
		if(m_lastMode == null || !m_lastMode.equals(mode))
		{
			mode.apply(this);
			m_lastMode = mode;
		}
	}
	
	public static abstract class PrimitiveMode
	{
		private PrimitiveMode() { }
		protected abstract void apply(PrimitiveShader shader);
	}
	
	public static final class PrimitiveTexture extends PrimitiveMode
	{
		private Texture m_texture;
		
		public PrimitiveTexture(Texture texture)
		{
			m_texture = texture;
		}
		
		@Override
		protected void apply(PrimitiveShader shader)
		{
			shader.setMode(ShaderMode.PrimitiveTexture);
			shader.setSourceTexture(m_texture);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((m_texture == null) ? 0 : m_texture.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PrimitiveTexture other = (PrimitiveTexture) obj;
			if (m_texture == null) {
				if (other.m_texture != null)
					return false;
			} else if (m_texture != other.m_texture)
				return false;
			return true;
		}
		
	}
	
	public static final class PrimitiveColour extends PrimitiveMode
	{
		private Color m_color;

		public PrimitiveColour(Color color)
		{
			m_color = color;
		}
		
		@Override
		protected void apply(PrimitiveShader shader)
		{
			shader.setMode(ShaderMode.PrimitiveColor);
			shader.setSourceColor(m_color);
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((m_color == null) ? 0 : m_color.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PrimitiveColour other = (PrimitiveColour) obj;
			if (m_color == null) {
				if (other.m_color != null)
					return false;
			} else if (!m_color.equals(other.m_color))
				return false;
			return true;
		}
	}
	
	public static final class ColorReplace extends PrimitiveMode
	{
		private Texture m_texture;
		private Color m_search;
		private Color m_replace;
		
		public ColorReplace(Texture texture, Color search, Color replace)
		{
			m_texture = texture;
			m_search = search;
			m_replace = replace;
		}
		
		@Override
		protected void apply(PrimitiveShader shader)
		{
			shader.setMode(ShaderMode.ColorReplace);
			shader.setSourceTexture(m_texture);
			shader.setSourceColor(m_replace);
			shader.setAuxColor(m_search);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((m_replace == null) ? 0 : m_replace.hashCode());
			result = prime * result
					+ ((m_search == null) ? 0 : m_search.hashCode());
			result = prime * result
					+ ((m_texture == null) ? 0 : m_texture.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColorReplace other = (ColorReplace) obj;
			if (m_replace == null) {
				if (other.m_replace != null)
					return false;
			} else if (!m_replace.equals(other.m_replace))
				return false;
			if (m_search == null) {
				if (other.m_search != null)
					return false;
			} else if (!m_search.equals(other.m_search))
				return false;
			if (m_texture == null) {
				if (other.m_texture != null)
					return false;
			} else if (!m_texture.equals(other.m_texture))
				return false;
			return true;
		}
	}
	
	public static final class ColorMap extends PrimitiveMode
	{
		private Texture m_source;
		private Texture m_map;
		private Color m_filter;
		
		public ColorMap(Texture source, Texture map, Color filter)
		{
			m_source = source;
			m_map = map;
			m_filter = filter;
		}
		
		@Override
		protected void apply(PrimitiveShader shader)
		{
			shader.setMode(ShaderMode.ColorMap);
			shader.setSourceTexture(m_source);
			shader.setAuxTexture(m_map);
			shader.setAuxColor(m_filter);
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((m_filter == null) ? 0 : m_filter.hashCode());
			result = prime * result + ((m_map == null) ? 0 : m_map.hashCode());
			result = prime * result
					+ ((m_source == null) ? 0 : m_source.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ColorMap other = (ColorMap) obj;
			if (m_filter == null) {
				if (other.m_filter != null)
					return false;
			} else if (!m_filter.equals(other.m_filter))
				return false;
			if (m_map == null) {
				if (other.m_map != null)
					return false;
			} else if (m_map != other.m_map)
				return false;
			if (m_source == null) {
				if (other.m_source != null)
					return false;
			} else if (m_source != other.m_source)
				return false;
			return true;
		}
	}
}
