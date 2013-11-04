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

public class Script
{
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
		return m_scope;
	}

	public final @Nullable Object invokeScriptFunction(String functionName, Object... arguments) throws NoSuchMethodException, ScriptException
	{
		if(m_scope == null)
			throw new NoSuchMethodException();
		
		Object function = m_scope.get(functionName, m_scope);
		
		if(function == ScriptableObject.NOT_FOUND || 
				!(function instanceof Function))
			throw new NoSuchMethodException();
		
		Object returnValue = Context.call(ContextFactory.getGlobal(), ((Function)function), m_scope, null, arguments);
		
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

	public class ScriptContext
	{
		public void evaluate(String path)
		{
			Script.this.evaluate(Core.getService(IResourceLibrary.class).openResourceContents(path));
		}
	}
}
