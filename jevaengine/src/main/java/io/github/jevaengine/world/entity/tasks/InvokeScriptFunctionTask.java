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
public final class InvokeScriptFunctionTask extends SynchronousOneShotTask
{
	private IFunction m_function;
	
	private Object[] m_arguments;

	private Logger m_logger = LoggerFactory.getLogger(InvokeScriptFunctionTask.class);
	
	public InvokeScriptFunctionTask(IFunction function, Object ... arguments)
	{
		m_function = function;
		m_arguments = arguments;
	}
	
	@Override
	public void run(IEntity entity)
	{
		try
		{
			m_function.call(m_arguments);
		} catch(ScriptExecuteException e)
		{
			m_logger.error("Error invoking callback task on entity" + entity.getInstanceName(), e);
		}
	}
}
