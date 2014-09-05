package io.github.jevaengine.script;

public class NullFunctionFactory implements IFunctionFactory
{
	@Override
	public IFunction wrap(Object function) throws UnrecognizedFunctionException
	{
		throw new UnrecognizedFunctionException();
	}

	@Override
	public boolean recognizes(Object function)
	{
		return false;
	}
	
}
