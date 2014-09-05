package io.github.jevaengine.script.rhino;

import io.github.jevaengine.script.IScriptArray;

import org.mozilla.javascript.NativeArray;

public final class RhinoArray implements IScriptArray
{
	private NativeArray m_array;

	public RhinoArray(NativeArray array)
	{
		m_array = array;
	}
	
	@Override
	public int getLength()
	{
		return (int)m_array.getLength();
	}

	@Override
	public Object getElement(int index)
	{
		return m_array.get(index, null);
	}
}
