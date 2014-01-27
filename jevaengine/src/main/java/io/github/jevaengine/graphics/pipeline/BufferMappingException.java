package io.github.jevaengine.graphics.pipeline;

public final class BufferMappingException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	BufferMappingException()
	{
		super("Error mapping buffer into memory for write");
	}
}