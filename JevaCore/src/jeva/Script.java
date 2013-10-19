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
package jeva;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import sun.org.mozilla.javascript.internal.Scriptable;
import jeva.game.Game;


public class Script
{
	/** The m_script engine. */
	private ScriptEngine m_scriptEngine;
	
	private Object m_context;
	
	public Script(Object context)
	{
		m_context = context;
	}
	
	private void initEngine()
	{
		if(m_scriptEngine == null)
		{
			m_scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	
			if (m_scriptEngine == null)
				throw new CoreScriptException("JavaScript script engine is not installed.");
	
			m_scriptEngine.put("core", Core.getScriptBridge());
			m_scriptEngine.put("game", Core.getService(Game.class).getScriptBridge().getGameBridge());
			m_scriptEngine.put("script", new ScriptContext());
			m_scriptEngine.put("me", m_context);
			
			for (Map.Entry<String, Object> entry : Core.getService(Game.class).getScriptBridge().getGlobals().entrySet())
				m_scriptEngine.put(entry.getKey(), entry.getValue());
		}
	}
	
	public Script()
	{
		this(null);
	}

	public final Scriptable getScriptedInterface()
	{
		return new ScriptInterface();
	}
	
	public final Object invokeScriptFunction(String functionName, Object... arguments) throws NoSuchMethodException, ScriptException
	{
		if(m_scriptEngine == null)
			throw new NoSuchMethodException("Script has not been initialized with anything and thus does not contain this method");
		
		return ((Invocable) m_scriptEngine).invokeFunction(functionName, arguments);
	}
	
	public final Object evaluate(String expression)
	{
		try
		{
			initEngine();
			return m_scriptEngine.eval(expression);
		} catch (ScriptException e)
		{
			throw new CoreScriptException(e);
		}
	}
	
	public class ScriptContext
	{
		public void evaluate(String path)
		{
			Script.this.evaluate(Core.getService(IResourceLibrary.class).openResourceContents(path));
		}
	}
	
	public class ScriptInterface implements Scriptable
	{
		@Override
		public void delete(String arg0) { }

		@Override
		public void delete(int arg0) { }

		@Override
		public Object get(String name, Scriptable start)
		{
			initEngine();
			return m_scriptEngine.get(name);
		}

		@Override
		public Object get(int index, Scriptable start)
		{
			return null;
		}

		@Override
		public String getClassName()
		{
			return "Script";
		}

		@Override
		public Object getDefaultValue(Class<?> arg0)
		{
			return null;
		}

		@Override
		public Object[] getIds()
		{
			return null;
		}

		@Override
		public Scriptable getParentScope()
		{
			return null;
		}

		@Override
		public Scriptable getPrototype()
		{
			return null;
		}

		@Override
		public boolean has(String arg0, Scriptable arg1)
		{
			try
			{
				initEngine();
				m_scriptEngine.get(arg0);
				return true;
			} catch(IllegalArgumentException | NullPointerException e)
			{
				return false;
			}
		}

		@Override
		public boolean has(int arg0, Scriptable arg1)
		{
			return false;
		}

		@Override
		public boolean hasInstance(Scriptable arg0)
		{
			return false;
		}

		@Override
		public void put(String key, Scriptable arg1, Object value)
		{
			initEngine();
			m_scriptEngine.put(key, value);
		}

		@Override
		public void put(int arg0, Scriptable arg1, Object arg2) { }

		@Override
		public void setParentScope(Scriptable arg0) { }

		@Override
		public void setPrototype(Scriptable arg0) { }
	}
}
