/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.script.jython;

import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScript;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.jython.PyUtil.UnrecognizedPythonPrimitiveException;
import io.github.jevaengine.script.rhino.RhinoQueue;
import io.github.jevaengine.util.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrapFactory;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PythonScript implements IScript
{
	static
	{
		PySystemState.initialize();
	}
	
	private final Logger m_logger = LoggerFactory.getLogger(PythonScript.class);
	
	private PythonInterpreter m_scope;
	private final String m_name;
	
	public PythonScript(String name)
	{
		m_name = name;
	}
	
	private void initEngine()
	{
		if (m_scope == null)
			m_scope = new PythonInterpreter();
	}

	public void put(String name, Object o)
	{
		initEngine();
		m_scope.set(name, o);
	}
	
	@Override
	public IFunctionFactory getFunctionFactory()
	{
		return new PythonFunctionFactory();
	}
	
	@Nullable
	public final Object evaluate(String expression) throws ScriptExecuteException
	{
		initEngine();
		try {
			return PyUtil.pyPrimitiveToJava(m_scope.eval(expression));
		} catch (UnrecognizedPythonPrimitiveException e) {
			throw new ScriptExecuteException(e);
		}
	}

	private static class ProtectedContextFactory extends ContextFactory
	{
		private static final ProtectedWrapFactory wrapper = new ProtectedWrapFactory();
		
		@Override
		protected Context makeContext()
		{
			Context c = super.makeContext();
			c.setWrapFactory(wrapper);
			
			return c;
		}
	}
	
	private static class ProtectedWrapFactory extends WrapFactory
	{
		@Override
		public Scriptable wrapAsJavaObject(Context cx, Scriptable scope, Object javaObject, Class<?> staticType)
		{
			return new ProtectedNativeJavaObject(scope, javaObject, staticType);
		}
	}
	
	private static class ProtectedNativeJavaObject extends NativeJavaObject
	{
		private static final long serialVersionUID = 1L;

		private static final HashMap<Class<?>, ArrayList<String>> CLASS_PROTECTION_CACHE = new HashMap<Class<?>, ArrayList<String>>();
		
		private ArrayList<String> m_protectedMembers;
		
		public ProtectedNativeJavaObject(Scriptable scope, Object javaObject, Class<?> staticType)
		{
			super(scope, javaObject, staticType);
			
			Class<?> clazz = javaObject != null ? javaObject.getClass() : staticType;
			
			m_protectedMembers = CLASS_PROTECTION_CACHE.get(clazz);
			
			if(m_protectedMembers == null)
				m_protectedMembers = processClass(clazz);
		}
		
		private static ArrayList<String> processClass(Class<?> clazz)
		{
			ArrayList<String> protectedMethods = new ArrayList<String>();
			
			CLASS_PROTECTION_CACHE.put(clazz, protectedMethods);

			for(Method m : clazz.getMethods())
			{
				if(m.getAnnotation(ScriptHiddenMember.class) != null)
					protectedMethods.add(m.getName());
			}
			
			for(Field f : clazz.getFields())
			{
				if(f.getAnnotation(ScriptHiddenMember.class) != null)
					protectedMethods.add(f.getName());
			}
			return protectedMethods;
		}
		
		@Override
		public boolean has(String name, Scriptable start)
		{
			if(m_protectedMembers.contains(name))
				return false;
			else
				return super.has(name, start);
		}
		
		@Override
		public Object get(String name, Scriptable start)
		{
			if(m_protectedMembers.contains(name))
				return NOT_FOUND;
			else
				return super.get(name, start);
		}
	}
}
