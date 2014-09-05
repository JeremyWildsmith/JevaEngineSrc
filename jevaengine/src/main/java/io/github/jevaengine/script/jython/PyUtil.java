package io.github.jevaengine.script.jython;

import org.python.core.Py;
import org.python.core.PyArray;
import org.python.core.PyBoolean;
import org.python.core.PyFloat;
import org.python.core.PyInteger;
import org.python.core.PyLong;
import org.python.core.PyObject;
import org.python.core.PyString;

public class PyUtil
{
	public static Object pyPrimitiveToJava(PyObject o) throws UnrecognizedPythonPrimitiveException
	{
		if(o.equals(Py.None))
			return null;
		else if(o instanceof PyArray)
			return new PythonArray((PyArray)o);
		else if(o instanceof PyBoolean)
			return new Boolean(((PyBoolean)o).getBooleanValue());
		else if(o instanceof PyInteger)
			return new Integer(((PyInteger)o).getValue());
		else if(o instanceof PyLong)
			return new Long(((PyLong)o).getValue().longValue());
		else if(o instanceof PyString)
			return new String(((PyString)o).getString());
		else if(o instanceof PyFloat)
			return new Float(((PyFloat)o).getValue());
		
		throw new UnrecognizedPythonPrimitiveException();
		
	}
	
	public static final class UnrecognizedPythonPrimitiveException extends Exception
	{
		private static final long serialVersionUID = 1L;
	
		private UnrecognizedPythonPrimitiveException() { }
	}
}
