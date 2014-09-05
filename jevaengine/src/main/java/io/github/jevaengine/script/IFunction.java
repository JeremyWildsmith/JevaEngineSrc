package io.github.jevaengine.script;

public interface IFunction
{
	Object call(Object ... arguments) throws ScriptExecuteException;
}
