package io.github.jevaengine.script;


public final class NullScriptFactory implements IScriptFactory
{
	@Override
	public IScript create(Object context, String name) throws ScriptConstructionException
	{
		return new NullScript();
	}

	@Override
	public IScript create() throws ScriptConstructionException
	{
		return new NullScript();
	}
	
	@Override
	public IFunctionFactory getFunctionFactory()
	{
		return new NullFunctionFactory();
	}
}
