package io.github.jevaengine.graphics.pipeline;

import java.awt.Color;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Scanner;

import javax.media.opengl.GL2;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.math.Vector4D;
import io.github.jevaengine.math.Vector4F;
import io.github.jevaengine.util.Nullable;

final class GraphicShader implements IDisposable
{
	
	private String m_vertexShaderSource = new String();
	private String m_fragmentShaderSource = new String();
	
	private boolean m_isActive = false;
	private boolean m_isDirty = true;
	
	private int m_vertexShader = 0;
	private int m_fragmentShader = 0;
	private int m_program = 0;
	
	private int m_lastProgram = 0;
	
	@Nullable
	private GL2 m_glContext;
	
	public GraphicShader()
	{
	}
	
	@Override
	public void dispose()
	{
		cleanup();
	}
	
	private static void compileShader(GL2 glContext, int shader, String source)
	{
		IntBuffer intBuffer = IntBuffer.allocate(1);
		
		//Compile vertex shader
		intBuffer.array()[0] = source.length();
		glContext.glShaderSource(shader, 1, new String[] {source}, intBuffer);
		glContext.glCompileShader(shader);
		
		glContext.glGetShaderiv(shader, GL2.GL_COMPILE_STATUS, intBuffer);
		
		if(intBuffer.get(0) != GL2.GL_TRUE)
		{
			glContext.glGetShaderiv(shader, GL2.GL_INFO_LOG_LENGTH, intBuffer);
			
			if(intBuffer.get(0) > 0)
			{
				ByteBuffer reason = ByteBuffer.allocate(intBuffer.get(0));
				glContext.glGetShaderInfoLog(shader, intBuffer.get(0), intBuffer, reason);
			
				throw new ShaderCompileException(reason, intBuffer.get(0));
			
			}else
				throw new ShaderCompileException();
		}
	}
	
	private static void linkProgram(GL2 glContext, int program, int[] shaders)
	{
		for(int shader : shaders)
			glContext.glAttachShader(program, shader);
		
		glContext.glLinkProgram(program);
		
		//Query program link status:
		IntBuffer intBuffer = IntBuffer.allocate(1);
		glContext.glValidateProgram(program);
		glContext.glGetProgramiv(program, GL2.GL_LINK_STATUS, intBuffer);
		
		if(intBuffer.get(0) != GL2.GL_TRUE)
		{
			glContext.glGetProgramiv(program, GL2.GL_INFO_LOG_LENGTH, intBuffer);
			
			if(intBuffer.get(0) > 0)
			{
				ByteBuffer reason = ByteBuffer.allocate(intBuffer.get(0));
				glContext.glGetProgramInfoLog(program, intBuffer.get(0), intBuffer, reason);
			
				throw new ProgramLinkException(reason, intBuffer.get(0));
			
			}else
				throw new ProgramLinkException();
		}
	}
	
	private void createShaders()
	{
		if(m_glContext == null)
			throw new InvalidShaderStateException();

		m_vertexShader = m_glContext.glCreateShader(GL2.GL_VERTEX_SHADER);
		if(m_vertexShader == 0)
			throw new ShaderException("Failed to create fragment or pixel shader.");

		compileShader(m_glContext, m_vertexShader, m_vertexShaderSource);
		
		m_fragmentShader = m_glContext.glCreateShader(GL2.GL_FRAGMENT_SHADER);
		if(m_fragmentShader == 0)
			throw new ShaderException("Failed to create fragment or pixel shader.");

		compileShader(m_glContext, m_fragmentShader, m_fragmentShaderSource);
		
		//Link to program
		m_program = m_glContext.glCreateProgram();
		
		if(m_program == 0)
			throw new ShaderException("Failed to create shader program");

		linkProgram(m_glContext, m_program, new int[] {m_vertexShader, m_fragmentShader});
		
		m_isDirty = false;
	}
	
	private void cleanup()
	{
		if(m_glContext == null)
			return;
		
		if(m_program != 0)
		{
			m_glContext.glUseProgram(0);
			m_glContext.glDeleteProgram(m_program);
			m_program = 0;
		}
		
		if(m_vertexShader != 0)
		{
			m_glContext.glDeleteShader(m_vertexShader);
			m_vertexShader = 0;
		}
		
		if(m_fragmentShader != 0)
		{
			m_glContext.glDeleteShader(m_vertexShader);
			m_fragmentShader = 0;
		}
		
		m_isDirty = true;
	}
	
	/*
	 * Standardize new-line format fed to OpenGL.
	 */
	private static String parseShader(String shader)
	{
		StringBuilder sb = new StringBuilder();
		
		try(Scanner scanner = new Scanner(shader))
		{
			while(scanner.hasNextLine())
				sb.append(scanner.nextLine() + "\n");
		}
		
		return sb.toString();
	}
	
	private static String readAll(InputStream is, String encoding)
	{
		try(Scanner scanner = new Scanner(is, encoding))
		{
			scanner.useDelimiter("\\A");
	
			return (scanner.hasNext() ? scanner.next() : "");
		}
	}

	public void loadVertexShader(InputStream shader, String encoding)
	{
		loadVertexShader(readAll(shader, encoding));
	}
	
	public void loadFragmentShader(InputStream shader, String encoding)
	{
		loadFragmentShader(readAll(shader, encoding));
	}
	
	public void loadVertexShader(String shader)
	{
		if(m_isActive)
			throw new InvalidShaderStateException();
		
		m_vertexShaderSource = parseShader(shader);
		m_isDirty = true;
	}
	
	public void loadFragmentShader(String shader)
	{
		if(m_isActive)
			throw new InvalidShaderStateException();
		
		m_fragmentShaderSource = parseShader(shader);
		m_isDirty = true;
	}
	
	public void prepare(GL2 glContext)
	{
		if(m_glContext != glContext)
		{
			cleanup();
			m_glContext = glContext;
		}
		
		if(m_isDirty)
		{
			m_glContext = glContext;
			createShaders();
		}
	}
	
	public void begin(GL2 glContext)
	{
		if(m_isActive)
			return;
		
		prepare(glContext);
		
		IntBuffer buffer = IntBuffer.allocate(1);
		glContext.glGetIntegerv(GL2.GL_CURRENT_PROGRAM, buffer);
		m_lastProgram = buffer.get(0);
		
		m_glContext.glUseProgram(m_program);
		m_isActive = true;
	}
	
	public void end()
	{
		if(!m_isActive)
			return;
		
		m_glContext.glUseProgram(m_lastProgram);
		m_isActive = false;
	}
	
	private int getUniformLocation(String name)
	{
		int location = m_glContext.glGetUniformLocationARB(m_program, name);
		
		if(location < 0)
			throw new ShaderUniformNotFoundException(name);
		
		return location;
	}

	public void setUniform1D(String name, int value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform1i(location, value);
	}
	
	public void setUniform1F(String name, float value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform1f(location, value);
	}
	
	public void setUniform2D(String name, Vector2D value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform2i(location, value.x, value.y);
	}
	
	public void setUniform2F(String name, Vector2F value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform2f(location, value.x, value.y);
	}
	
	public void setUniform2D(String name, Vector3D value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform3i(location, value.x, value.y, value.z);
	}
	
	public void setUniform3F(String name, Vector3F value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform3f(location, value.x, value.y, value.z);
	}

	public void setUniform4F(String name, Vector4F value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform4f(location, value.x, value.y, value.z, value.w);
	}

	public void setUniform4D(String name, Vector4D value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform4i(location, value.x, value.y, value.z, value.w);
	}
	
	public void setUniformColor(String name, Color value)
	{
		int location = getUniformLocation(name);
		
		m_glContext.glUniform4f(location, (float)value.getRed()/255.0F, (float)value.getGreen()/255.0F, (float)value.getBlue()/255.0F, (float)value.getAlpha()/255.0F);
	}
	
	public static class ShaderException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		private ShaderException(String reason)
		{
			super(reason);
		}
	}

	public static final class ShaderUniformNotFoundException extends ShaderException
	{
		private static final long serialVersionUID = 1L;
		
		private ShaderUniformNotFoundException(String name)
		{
			super("Unable to locate uniform: " + name);
		}
	}
	
	public static final class ShaderCompileException extends ShaderException
	{
		private static final long serialVersionUID = 1L;

		private ShaderCompileException()
		{
			super("Shader failed to compile for unknown reasons");
		}
		
		private ShaderCompileException(ByteBuffer log, int length)
		{
			super("Shader failed to compile: " + parseLog(log, length));
		}
		
		private static String parseLog(ByteBuffer buffer, int length)
		{
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < length; i++)
				sb.append((char)buffer.get(i));
			
			return sb.toString();
		}
	}
	
	public static final class ProgramLinkException extends ShaderException
	{
		private static final long serialVersionUID = 1L;

		private ProgramLinkException()
		{
			super("Shader failed to compile for unknown reasons");
		}
		
		private ProgramLinkException(ByteBuffer log, int length)
		{
			super("Shader failed to compile: " + parseLog(log, length));
		}
		
		private static String parseLog(ByteBuffer buffer, int length)
		{
			StringBuilder sb = new StringBuilder();
			
			for(int i = 0; i < length; i++)
				sb.append((char)buffer.get(i));
			
			return sb.toString();
		}
	}
	
	public static final class InvalidShaderStateException extends ShaderException
	{
		private static final long serialVersionUID = 1L;

		private InvalidShaderStateException()
		{
			super("Operation cannot be performed in this state");
		}
	}
}
