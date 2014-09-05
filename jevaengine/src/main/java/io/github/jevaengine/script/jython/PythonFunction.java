package io.github.jevaengine.script.jython;

import io.github.jevaengine.script.IFunction;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.jython.PyUtil.UnrecognizedPythonPrimitiveException;

import org.python.core.Py;
import org.python.core.PyObject;

public class PythonFunction implements IFunction
{
	private PyObject m_function;
	
	public PythonFunction(PyObject function)
	{
		m_function = function;
	}

	@Override
	public Object call(final Object... arguments) throws ScriptExecuteException
	{
		try
		{
			PyObject pyArguments[] = new PyObject[arguments.length];
			
			for(int i = 0; i < arguments.length; i++)
				pyArguments[i] = Py.java2py(arguments[i]);
			
			return PyUtil.pyPrimitiveToJava(m_function.__call__(pyArguments));
			
		} catch(RuntimeException | UnrecognizedPythonPrimitiveException e)
		{
			throw new ScriptExecuteException(e);
		}
	}
}
