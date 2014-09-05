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

import io.github.jevaengine.world.entity.Actor;
import io.github.jevaengine.world.entity.DefaultEntity;
import io.github.jevaengine.world.entity.IEntity;

public abstract class SearchForTask<T extends DefaultEntity> implements ITask
{
	private static final int SEARCH_INTERVAL = 400;

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

	@Override
	public final void cancel()
	{
		m_isQueryCancel = true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean doCycle(int deltaTime)
	{
		if (m_isQueryCancel)
			return true;

		m_nextSearch -= deltaTime;

		if (m_nextSearch <= 0)
		{
			m_nextSearch = SEARCH_INTERVAL;

			boolean foundEntity = false;

			for (DefaultEntity e : m_searcher.getVisibleEntities())
			{
				if (m_seekingClass.isAssignableFrom(e.getClass()) && (m_searchEntityName == null ? true : e.getInstanceName().compareTo(m_searchEntityName) == 0))
				{
					foundEntity = true;
					found((T)e);
				}
			}

			if (!foundEntity)
				nothingFound();
		}

		return !continueSearch();
	}

	@Override
	public final boolean isParallel()
	{
		return true;
	}

	@Override
	public final void begin(IEntity entity)
	{
		m_isQueryCancel = false;
	}

	@Override
	public final void end() { }

	public abstract void found(T entity);

	public abstract void nothingFound();

	public abstract boolean continueSearch();
}
