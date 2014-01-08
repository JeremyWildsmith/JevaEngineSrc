/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.world;

import io.github.jevaengine.CoreScriptException;
import javax.script.ScriptException;
import org.mozilla.javascript.Function;

/**
 *
 * @author Jeremy
 */
public final class InvokeScriptTimeoutFunctionTask implements ITask
{
	private Function m_function;
	private Object[] m_arguments;
	
	private Entity m_entity;
	
	private int m_timeout;
	private boolean m_queryCancel = false;
	
	public InvokeScriptTimeoutFunctionTask(int timeout, Function function, Object ... arguments)
	{
		m_timeout = timeout;
		m_function = function;
		m_arguments = arguments;
	}

	@Override
	public void cancel()
	{
		m_queryCancel = true;
	}

	@Override
	public void begin(Entity entity)
	{
		m_queryCancel = false;
		m_entity = entity;
	}

	@Override
	public void end()
	{ }

	@Override
	public boolean doCycle(int deltaTime)
	{
		if(m_queryCancel)
			return true;
	
		m_timeout -= deltaTime;
		
		if(m_timeout <= 0)
		{
			try
			{
				m_entity.getScript().invokeScriptFunction(m_function, m_arguments);
			} catch (NoSuchMethodException | ScriptException ex)
			{
				throw new CoreScriptException(ex);
			}
			return true;
		}else
			return false;
		
	}

	@Override
	public boolean isParallel()
	{
		return true;
	}

	@Override
	public boolean ignoresPause()
	{
		return false;
	}
	
}
