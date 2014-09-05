package io.github.jevaengine.script.rhino;

import io.github.jevaengine.script.IFunction;
import io.github.jevaengine.script.ScriptExecuteException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeArray;

public class RhinoFunction implements IFunction
{
	private Function m_rhinoFunction;
	
	public RhinoFunction(Function function)
	{
		m_rhinoFunction = function;
	}

	@Override
	public Object call(final Object... arguments) throws ScriptExecuteException
	{
		try
		{
			Object oReturn = ContextFactory.getGlobal().call(new ContextAction() {
				@Override
				public Object run(Context cx) {
					return m_rhinoFunction.call(cx, m_rhinoFunction.getParentScope(), null, arguments);
				}
			});
			
			if(oReturn instanceof NativeArray)
				return new RhinoArray((NativeArray)oReturn);
			else
				return oReturn;
		} catch(RuntimeException e)
		{
			throw new ScriptExecuteException(e);
		}
	}
}
