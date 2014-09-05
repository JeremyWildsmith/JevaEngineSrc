package io.github.jevaengine.script.jython;

import io.github.jevaengine.script.IFunction;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.UnrecognizedFunctionException;

import org.python.core.PyObject;

public class PythonFunctionFactory implements IFunctionFactory
{
	@Override
	public IFunction wrap(Object function) throws UnrecognizedFunctionException
	{
		if(function instanceof PyObject && ((PyObject)function).isCallable())
			return new PythonFunction((PyObject)function);
		else
			throw new UnrecognizedFunctionException();
	}

	@Override
	public boolean recognizes(Object function)
	{
		return function instanceof PyObject && ((PyObject)function).isCallable();
	}
}
