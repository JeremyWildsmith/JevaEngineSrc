package io.github.jevaengine.config;

public final class NoSuchChildVariableException extends Exception
{
	private static final long serialVersionUID = 1L;

	public NoSuchChildVariableException(String name)
	{
		super("Child does not exist with the name: " + name);
	}
	
}
