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

public abstract class SearchForTask<T extends Entity> implements ITask
{

	private static final int SEARCH_INTERVAL = 600;

	Actor m_searcher;

	private Class<T> m_seekingClass;

	private int m_nextSearch;

	private boolean m_isQueryCancel;

	private String m_searchEntityName;

	public SearchForTask(Actor searcher, Class<T> seekingClass)
	{
		m_searcher = searcher;
		m_seekingClass = seekingClass;
		m_isQueryCancel = false;
		m_nextSearch = 0;
		m_searchEntityName = null;
	}

	public SearchForTask(Actor searcher, Class<T> seekingClass, String name)
	{
		m_searcher = searcher;
		m_seekingClass = seekingClass;
		m_isQueryCancel = false;
		m_nextSearch = 0;
		m_searchEntityName = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_isQueryCancel = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#doCycle(int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public boolean doCycle(int deltaTime)
	{
		if (m_isQueryCancel)
			return true;

		m_nextSearch -= deltaTime;

		if (m_nextSearch <= 0)
		{
			m_nextSearch = SEARCH_INTERVAL;

			boolean foundEntity = false;

			for (Entity e : m_searcher.getVisibleEntities())
			{
				if (m_seekingClass.isAssignableFrom(e.getClass()) && (m_searchEntityName == null ? true : e.getName().compareTo(m_searchEntityName) == 0))
				{
					foundEntity = true;
					if (found((T) e))
						break;
				}
			}

			if (!foundEntity)
				nothingFound();
		}

		return !continueSearch();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#isParallel()
	 */
	@Override
	public boolean isParallel()
	{
		return true;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{

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

	public abstract boolean found(T entity);

	public abstract void nothingFound();

	public abstract boolean continueSearch();

}
