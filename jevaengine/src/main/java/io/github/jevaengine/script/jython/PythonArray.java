package io.github.jevaengine.script.jython;

import io.github.jevaengine.script.IScriptArray;
import io.github.jevaengine.script.jython.PyUtil.UnrecognizedPythonPrimitiveException;

import org.python.core.PyArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PythonArray implements IScriptArray
{
	private final Logger m_logger = LoggerFactory.getLogger(PythonArray.class);
	private PyArray m_array;
	public PythonArray(PyArray array)
	{
		m_array = array;
	}
	
	@Override
	public int getLength()
	{
		return m_array.__len__();
	}

	@Override
	public Object getElement(int index)
	{
		try {
			return PyUtil.pyPrimitiveToJava(m_array.__getitem__(index));
		} catch (UnrecognizedPythonPrimitiveException e) {
			m_logger.error("Could not convert python array element to java counterpart, using null element value instead", e);
			return null;
		}
	}
}
