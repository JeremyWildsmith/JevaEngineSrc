package io.github.jevaengine.ui;

public final class NoSuchControlException extends Exception
{
	private static final long serialVersionUID = 1L;

	public NoSuchControlException(Class<?> componentClass, String componentName)
	{
		super("Behaviour missing required component " + componentName + " of class " + componentClass.getName());
	}
}