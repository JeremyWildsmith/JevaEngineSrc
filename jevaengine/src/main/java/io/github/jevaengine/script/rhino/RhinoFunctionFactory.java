package io.github.jevaengine.script.rhino;

import org.mozilla.javascript.Function;

import io.github.jevaengine.script.IFunction;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.UnrecognizedFunctionException;

public class RhinoFunctionFactory implements IFunctionFactory
{
	@Override
	public IFunction wrap(Object function) throws UnrecognizedFunctionException
	{
		if(function instanceof Function)
			return new RhinoFunction((Function)function);
		else
			throw new UnrecognizedFunctionException();
	}

	@Override
	public boolean recognizes(Object function)
	{
		return function instanceof Function;
	}
}
