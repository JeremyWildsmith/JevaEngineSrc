package io.github.jevaengine.script;

public interface IFunctionFactory
{
	IFunction wrap(Object function) throws UnrecognizedFunctionException;
	boolean recognizes(Object function);
}
