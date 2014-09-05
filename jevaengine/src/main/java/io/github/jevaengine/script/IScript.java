package io.github.jevaengine.script;

import io.github.jevaengine.util.Nullable;

public interface IScript
{
	void put(String name, Object o);

	IFunctionFactory getFunctionFactory();
	
	@Nullable
	Object evaluate(String expression) throws ScriptExecuteException;
}
