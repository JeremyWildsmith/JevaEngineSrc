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
package io.github.jevaengine.world;

public class IdleTask implements ITask
{

	private int m_idleLength;

	private int m_idleTime;

	public IdleTask(int idleTime)
	{
		m_idleLength = idleTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{
		m_idleTime = m_idleLength;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#end()
	 */
	@Override
	public void end()
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	@Override
	public boolean doCycle(int deltaTime)
	{
		m_idleTime -= deltaTime;

		return (m_idleTime <= 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_idleTime = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#isParallel()
	 */
	@Override
	public final boolean isParallel()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#ignoresPause()
	 */
	@Override
	public boolean ignoresPause()
	{
		return false;
	}

}
