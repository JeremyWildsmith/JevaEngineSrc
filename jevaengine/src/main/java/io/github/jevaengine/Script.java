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
package io.github.jevaengine;

import java.util.Map;

import javax.script.ScriptException;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

import io.github.jevaengine.game.Game;
import io.github.jevaengine.util.Nullable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.WrapFactory;

public class Script
{
	static
	{
		ContextFactory.initGlobal(new ProtectedContextFactory());
	}
	
	private ScriptableObject m_scope;
	private Object m_context;
	
	public Script(Object context)
	{
		m_context = context;
	}

	private void initEngine()
	{
		if (m_scope == null)
		{
			Context context = ContextFactory.getGlobal().enterContext();
			m_scope = context.initStandardObjects();
			Context.exit();

			m_scope.putConst("core", m_scope, Core.getScriptBridge());
			m_scope.putConst("game", m_scope, Core.getService(Game.class).getScriptBridge().getGameBridge());
			m_scope.putConst("script", m_scope, new ScriptContext());
			m_scope.putConst("me", m_scope, m_context);

			for (Map.Entry<String, Object> entry : Core.getService(Game.class).getScriptBridge().getGlobals().entrySet())
				m_scope.putConst(entry.getKey(), m_scope, entry.getValue());
		}
	}

	public Script()
	{
		this(null);
	}
	
	public final Scriptable getScriptedInterface()
	{
		initEngine();
		return m_scope;
	}

	public void putConst(String name, Object o)
	{
		initEngine();
		m_scope.putConst(name, m_scope, o);
	}
	
	public final @Nullable Object invokeScriptFunction(String functionName, Object... arguments) throws NoSuchMethodException, ScriptException
	{
		if(m_scope == null)
			throw new NoSuchMethodException();
		
		Object function = m_scope.get(functionName, m_scope);
		
		if(function == ScriptableObject.NOT_FOUND || 
				!(function instanceof Function))
			throw new NoSuchMethodException();
		
		return invokeScriptFunction(((Function)function), arguments);
	}
	
	public final @Nullable Object invokeScriptFunction(Function function, Object... arguments) throws NoSuchMethodException, ScriptException
	{
		if(m_scope == null)
			throw new NoSuchMethodException();
		
		Object returnValue = Context.call(ContextFactory.getGlobal(), function, m_scope, null, arguments);
		
		return returnValue instanceof Undefined ? null : returnValue;
	}

	public final @Nullable Object evaluate(String expression)
	{

		initEngine();
		Context context = ContextFactory.getGlobal().enterContext();
		
		try
		{
			Object returnValue = context.evaluateString(m_scope, expression, "JevaEngine", 0, null);
			
			return returnValue instanceof Undefined ? null : returnValue;
		} catch (Exception e)
		{
			throw new CoreScriptException(e);
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
		/**
		 * 
		 */
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
	
	@Target({ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ScriptHiddenMember {}
	
	public class ScriptContext
	{
		public void evaluate(String path)
		{
			Script script = Core.getService(ResourceLibrary.class).openScript(path, m_context);
			
			script.m_scope.setParentScope(getScriptedInterface());
			m_scope = script.m_scope;
		}
	}
}
