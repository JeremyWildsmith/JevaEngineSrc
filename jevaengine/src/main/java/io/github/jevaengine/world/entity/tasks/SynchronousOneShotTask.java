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
package io.github.jevaengine.world.entity.tasks;

import io.github.jevaengine.world.entity.IEntity;

public abstract class SynchronousOneShotTask implements ITask
{

	private IEntity m_entity;
	private boolean m_queryCancel = false;
	
	@Override
	public final void cancel()
	{
		m_queryCancel = true;
	}


	@Override
	public final void begin(IEntity entity)
	{
		m_entity = entity;
	}

	@Override
	public final void end()
	{
	}

	@Override
	public final boolean doCycle(int deltaTime)
	{
		if(!m_queryCancel)
			run(m_entity);
		
		return true;
	}

	@Override
	public final boolean isParallel()
	{
		return false;
	}

	public abstract void run(IEntity world);

}
