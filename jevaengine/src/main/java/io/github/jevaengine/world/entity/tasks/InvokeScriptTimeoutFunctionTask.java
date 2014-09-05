/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.world.entity.tasks;

import io.github.jevaengine.script.IFunction;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.world.entity.IEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeremy
 */
public final class InvokeScriptTimeoutFunctionTask implements ITask
{
	private IFunction m_function;
	private Object[] m_arguments;

	private int m_timeout;
	private boolean m_queryCancel = false;

	private IEntity m_entity;
	
	private final Logger m_logger = LoggerFactory.getLogger(InvokeScriptTimeoutFunctionTask.class);
	
	public InvokeScriptTimeoutFunctionTask(int timeout, IFunction function, Object ... arguments)
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
	public void begin(IEntity entity)
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
				m_function.call(m_arguments);
			} catch (ScriptExecuteException e)
			{
				m_logger.error("Error invoking timeout callback task on entity" + m_entity.getInstanceName(), e);
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
}
