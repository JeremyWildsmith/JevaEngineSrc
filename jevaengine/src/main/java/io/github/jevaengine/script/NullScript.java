package io.github.jevaengine.script;

public final class NullScript implements IScript
{

	@Override
	public void put(String name, Object o) { }

	@Override
	public IFunctionFactory getFunctionFactory()
	{
		return new NullFunctionFactory();
	}

	@Override
	public Object evaluate(String expression) throws ScriptExecuteException
	{
		return null;
	}
}
