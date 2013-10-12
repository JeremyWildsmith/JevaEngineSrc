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
package jeva.world;

/**
 * The Class SynchronousOneShotTask.
 */
public abstract class SynchronousOneShotTask implements ITask
{

	/** The m_entity. */
	private Entity m_entity;

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{
		m_entity = entity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#end()
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
		run(m_entity);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#isParallel()
	 */
	@Override
	public boolean isParallel()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#ignoresPause()
	 */
	@Override
	public boolean ignoresPause()
	{
		return false;
	}

	/**
	 * Run.
	 * 
	 * @param world
	 *            the world
	 */
	public abstract void run(Entity world);

}
