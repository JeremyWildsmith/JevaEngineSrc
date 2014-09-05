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
package io.github.jevaengine.script.rhino;

import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScript;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.ScriptHiddenMember;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RhinoScript implements IScript
{
	static
	{
		ContextFactory.initGlobal(new ProtectedContextFactory());
	}
	
	private final Logger m_logger = LoggerFactory.getLogger(RhinoScript.class);
	
	private ScriptableObject m_scope;
	private final String m_name;
	
	public RhinoScript(String name)
	{
		m_name = name;
	}
	
	private void initEngine()
	{
		if (m_scope == null)
		{
			Context context = ContextFactory.getGlobal().enterContext();
			m_scope = context.initStandardObjects();

			try
			{
				ScriptableObject.defineClass(m_scope, RhinoQueue.class);
			} catch (IllegalAccessException | InstantiationException
					| InvocationTargetException e) {
				m_logger.error("Unable to define Rhino queue class. Resuming without definition.", e);
			}
			
			Context.exit();
		}
	}
	
	public final Scriptable getScriptedInterface()
	{
		initEngine();
		return m_scope;
	}

	public void put(String name, Object o)
	{
		initEngine();
		m_scope.putConst(name, m_scope, o);
	}
	
	@Override
	public IFunctionFactory getFunctionFactory()
	{
		return new RhinoFunctionFactory();
	}
	
	@Nullable
	public final Object evaluate(String expression) throws ScriptExecuteException
	{
		initEngine();
		Context context = ContextFactory.getGlobal().enterContext();
		
		try
		{
			Object returnValue = context.evaluateString(m_scope, expression, "JevaEngine", 0, null);
			
			return returnValue instanceof Undefined ? null : returnValue;
		} catch (RuntimeException e)
		{
			throw new ScriptExecuteException(m_name, e);
		} finally
		{
			Context.exit();
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
