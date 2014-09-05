package io.github.jevaengine.script.rhino;

import java.util.LinkedList;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.ScriptableObject;

/*
 * Rhino engine missing javascript queue? Temporary fix...
 */

@Deprecated
public class RhinoQueue extends NativeJavaObject
{
	private static final long serialVersionUID = 1L;

	private RhinoQueue(ScriptableObject scope)
	{
		super(scope, new LinkedList<Object>(), LinkedList.class);
	}
	
	private RhinoQueue(Context cx)
	{
		this(cx.initStandardObjects());
		Context.exit();
	}
	
	public RhinoQueue()
	{
		this(Context.enter());
	}
	
	@Override
	public String getClassName()
	{
		return "Queue";
	}
}
