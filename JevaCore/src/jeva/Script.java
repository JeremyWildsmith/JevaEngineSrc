package jeva;

import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jeva.game.Game;

/**
 * This class describes a script executing inside the core and allows provides
 * mechanisms to interface with these scripts.
 */
public class Script
{
	/** The m_script engine. */
	private ScriptEngine m_scriptEngine;

	/**
	 * Instantiates a new script.
	 */
	public Script()
	{
	}

	/**
	 * Sets the script.
	 * 
	 * @param script
	 *            A plain-text representation of the script to load into the
	 *            Script entity.
	 * @param context
	 *            The context of the script, an object accessible via the 'me'
	 *            variable name to refer to a property of the object for which
	 *            the script is executing.
	 */
	public final void setScript(String script, Object context)
	{
		m_scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");

		if (m_scriptEngine == null)
			throw new CoreScriptException("JavaScript script engine is not installed.");

		m_scriptEngine.put("core", Core.getScriptBridge());
		m_scriptEngine.put("game", Core.getService(Game.class).getScriptBridge().getGameBridge());
		m_scriptEngine.put("me", context);

		for (Map.Entry<String, Object> entry : Core.getService(Game.class).getScriptBridge().getGlobals().entrySet())
			m_scriptEngine.put(entry.getKey(), entry.getValue());

		try
		{
			m_scriptEngine.eval(script);
		} catch (ScriptException e)
		{
			throw new CoreScriptException(e);
		}
	}

	/**
	 * Sets the script.
	 * 
	 * @param context
	 *            the new script
	 */
	public final void setScript(Object context)
	{
		setScript("", context);
	}

	/**
	 * Checks if is script ready.
	 * 
	 * @return true, if is script ready
	 */
	public final boolean isScriptReady()
	{
		return m_scriptEngine != null;
	}

	/**
	 * Invoke script function.
	 * 
	 * @param functionName
	 *            the function name
	 * @param arguments
	 *            the arguments
	 * @return the object
	 * @throws NoSuchMethodException
	 *             the no such method exception
	 * @throws ScriptException
	 *             the script exception
	 */
	public final Object invokeScriptFunction(String functionName, Object... arguments) throws NoSuchMethodException, ScriptException
	{
		if (!isScriptReady())
			throw new CoreScriptException("Cannot invoke script routine on script that is not ready");

		return ((Invocable) m_scriptEngine).invokeFunction(functionName, arguments);
	}

	/**
	 * Evaluate.
	 * 
	 * @param expression
	 *            the expression
	 * @return the object
	 * @throws ScriptException
	 *             the script exception
	 */
	public final Object evaluate(String expression) throws ScriptException
	{
		if (!isScriptReady())
			throw new CoreScriptException("Cannot invoke script routine on script that is not ready");

		try
		{
			return m_scriptEngine.eval(expression);
		} catch (ScriptException e)
		{
			throw new CoreScriptException(e);
		}
	}
}
