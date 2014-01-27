package io.github.jevaengine.graphics.pipeline;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.util.Nullable;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import javax.media.opengl.GL4;

public class DrawBatcher implements IDisposable
{
	private static final int BUFFER_SIZE = 200;
	
	private int m_enqueuedRenders = 0;

	private PrimitiveShader m_shader;
	private GL4 m_glContext;
	
	private ArrayList<DrawConfigurationEntry> m_renderQueue = new ArrayList<DrawConfigurationEntry>();

	private Clipping m_nextClip = new Clipping();
	
	private IntBuffer m_locationBuffer = IntBuffer.allocate(1);
	private IntBuffer m_texCoordBuffer = IntBuffer.allocate(1);
	
	public DrawBatcher(PrimitiveShader shader)
	{
		m_shader = shader;
	}
	
	@Override
	public void dispose()
	{
		cleanupBuffers();
	}
	
	private void cleanupBuffers()
	{
		if(m_glContext == null)
			return;
		
		if(m_locationBuffer.get(0) >= 0)
			m_glContext.glDeleteBuffers(1, m_locationBuffer);
		
		if(m_texCoordBuffer.get(0) >= 0)
			m_glContext.glDeleteBuffers(1, m_texCoordBuffer);
	}
	
	private DrawConfigurationEntry getConfigurationEntry(PrimitiveShader.PrimitiveMode shaderMode, Clipping clip)
	{
		if(m_renderQueue.isEmpty() ||
			!m_renderQueue.get(m_renderQueue.size() - 1).isCompatible(shaderMode, clip))
		{
			DrawConfigurationEntry entry = new DrawConfigurationEntry(shaderMode, clip);
			m_renderQueue.add(entry);
			return entry;
		}else
			return m_renderQueue.get(m_renderQueue.size() - 1);
	}
	
	public void setG2D(GL4 glContext)
	{
		cleanupBuffers();
		
		m_glContext = glContext;
		
		m_glContext.glGenBuffers(1, m_locationBuffer);
		m_glContext.glBindBuffer(GL4.GL_ARRAY_BUFFER, m_locationBuffer.get(0));
		m_glContext.glBufferData(GL4.GL_ARRAY_BUFFER, 8 * 6 * BUFFER_SIZE, null, GL4.GL_DYNAMIC_DRAW);

		m_glContext.glGenBuffers(1, m_texCoordBuffer);
		m_glContext.glBindBuffer(GL4.GL_ARRAY_BUFFER, m_texCoordBuffer.get(0));
		m_glContext.glBufferData(GL4.GL_ARRAY_BUFFER, 8 * 6 * BUFFER_SIZE, null, GL4.GL_DYNAMIC_DRAW);
	}
	
	public void draw(PrimitiveShader.PrimitiveMode mode, Rect2D destination, @Nullable Rect2F source)
	{
		if(m_enqueuedRenders >= BUFFER_SIZE)
			flush();
		
		m_enqueuedRenders++;
		
		DrawConfigurationEntry configurationEntry = getConfigurationEntry(mode, m_nextClip);
		configurationEntry.add(new BatchEntry(destination, source));
	}
	
	public void draw(PrimitiveShader.PrimitiveMode mode, Rect2D destination)
	{
		draw(mode, destination, null);
	}
	
	public void setClip(@Nullable Rect2D clip)
	{
		m_nextClip = new Clipping(clip);
	}
	
	public void flush()
	{
		m_glContext.glEnableVertexAttribArray(0);
		m_glContext.glEnableVertexAttribArray(1);

		m_shader.bindVertexPositionAttribute(0);
		m_shader.bindVertexTexCoordAttribute(1);
		
		//Bind and map location buffer
		m_glContext.glBindBuffer(GL4.GL_ARRAY_BUFFER, m_locationBuffer.get(0));
		m_glContext.glVertexAttribIPointer(0, 2, GL4.GL_INT, 0, 0L);
		ByteBuffer bbLocation = m_glContext.glMapBuffer(GL4.GL_ARRAY_BUFFER, GL4.GL_WRITE_ONLY);
		
		if(bbLocation == null)
			throw new BufferMappingException();
		
		//Bind and map Tex Coord Buffer
		m_glContext.glBindBuffer(GL4.GL_ARRAY_BUFFER, m_texCoordBuffer.get(0));
		m_glContext.glVertexAttribPointer(1, 2, GL4.GL_FLOAT, false, 0, 0L);
		ByteBuffer bbTexCoord = m_glContext.glMapBuffer(GL4.GL_ARRAY_BUFFER, GL4.GL_WRITE_ONLY);

		if(bbTexCoord == null)
			throw new BufferMappingException();

		IntBuffer locationBuffer = bbLocation.asIntBuffer();
		FloatBuffer texCoordBuffer = bbTexCoord.asFloatBuffer();
		
		//Fill Buffers
		for(DrawConfigurationEntry entry : m_renderQueue)
			entry.put(locationBuffer, texCoordBuffer);
		
		//Unmap buffers
		m_glContext.glBindBuffer(GL4.GL_ARRAY_BUFFER, m_locationBuffer.get(0));
		m_glContext.glUnmapBuffer(GL4.GL_ARRAY_BUFFER);
		
		m_glContext.glBindBuffer(GL4.GL_ARRAY_BUFFER, m_texCoordBuffer.get(0));
		m_glContext.glUnmapBuffer(GL4.GL_ARRAY_BUFFER);
		
		int base = 0;
		for(DrawConfigurationEntry entry : m_renderQueue)
			base = entry.render(base);
		
		m_renderQueue.clear();
		m_enqueuedRenders = 0;
	}
	
	private final class DrawConfigurationEntry
	{
		private PrimitiveShader.PrimitiveMode m_mode;
		private ArrayList<BatchEntry> m_entries;
		
		private Clipping m_clip;
		
		public DrawConfigurationEntry(PrimitiveShader.PrimitiveMode mode, Clipping clip)
		{
			m_mode = mode;
			m_clip = clip;
			m_entries = new ArrayList<BatchEntry>();
		}
		
		public void put(IntBuffer positionBuffer, FloatBuffer texCoordBuffer)
		{
			for(BatchEntry e: m_entries)
				e.put(positionBuffer, texCoordBuffer);
		}
		
		public boolean isCompatible(PrimitiveShader.PrimitiveMode mode, Clipping clip)
		{
			return mode.equals(m_mode) && clip.equals(m_clip);
		}
		
		public int render(int base)
		{
			m_clip.apply(m_glContext);
			m_shader.setShaderConfiguration(m_mode);
			m_glContext.glDrawArrays(GL4.GL_TRIANGLES, base, m_entries.size() * 6);
			return base + m_entries.size() * 6;
		}
		
		public void add(BatchEntry entry)
		{
			m_entries.add(entry);
		}
	}
	
	private static final class BatchEntry
	{
		private Rect2D m_destination;
		
		@Nullable
		private Rect2F m_sourceTextureArea;
		
		public BatchEntry(Rect2D destination, Rect2F sourceTextureArea)
		{
			m_destination = destination;
			m_sourceTextureArea = sourceTextureArea;
		}
		
		public void put(IntBuffer positionBuffer, FloatBuffer texCoordBuffer)
		{
			positionBuffer.put(m_destination.x);
			positionBuffer.put(m_destination.y + m_destination.height);

			positionBuffer.put(m_destination.x + m_destination.width);
			positionBuffer.put(m_destination.y + m_destination.height);

			positionBuffer.put(m_destination.x);
			positionBuffer.put(m_destination.y);
			
			//
			positionBuffer.put(m_destination.x + m_destination.width);
			positionBuffer.put(m_destination.y + m_destination.height);

			positionBuffer.put(m_destination.x);
			positionBuffer.put(m_destination.y);
			
			positionBuffer.put(m_destination.x + m_destination.width);
			positionBuffer.put(m_destination.y);
			
			if(m_sourceTextureArea != null)
			{
				texCoordBuffer.put(m_sourceTextureArea.x);
				texCoordBuffer.put(m_sourceTextureArea.y + m_sourceTextureArea.height);

				texCoordBuffer.put(m_sourceTextureArea.x + m_sourceTextureArea.width);
				texCoordBuffer.put(m_sourceTextureArea.y + m_sourceTextureArea.height);

				texCoordBuffer.put(m_sourceTextureArea.x);
				texCoordBuffer.put(m_sourceTextureArea.y);
				
				//
				texCoordBuffer.put(m_sourceTextureArea.x + m_sourceTextureArea.width);
				texCoordBuffer.put(m_sourceTextureArea.y + m_sourceTextureArea.height);

				texCoordBuffer.put(m_sourceTextureArea.x);
				texCoordBuffer.put(m_sourceTextureArea.y);
				
				texCoordBuffer.put(m_sourceTextureArea.x + m_sourceTextureArea.width);
				texCoordBuffer.put(m_sourceTextureArea.y);
			}else
			{
				for(int i = 0; i < 6 * 2; i++)
					texCoordBuffer.put(0);
			}
		}
	}
	
	private static class Clipping
	{
		@Nullable
		private Rect2D m_clip;
		
		public Clipping(@Nullable Rect2D clip)
		{
			m_clip = clip;
		}
		
		public Clipping()
		{
			this(null);
		}
		
		public void apply(GL4 glContext)
		{
			if(m_clip == null)
			{
				glContext.glDisable(GL4.GL_SCISSOR_TEST);
			}else
			{
				glContext.glEnable(GL4.GL_SCISSOR_TEST);
				glContext.glScissor(m_clip.x, m_clip.y, m_clip.width, m_clip.height);
			}
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((m_clip == null) ? 0 : m_clip.hashCode());
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
			Clipping other = (Clipping) obj;
			if (m_clip == null) {
				if (other.m_clip != null)
					return false;
			} else if (!m_clip.equals(other.m_clip))
				return false;
			return true;
		}
	}
}
