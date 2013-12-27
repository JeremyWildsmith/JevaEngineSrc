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
package io.github.jevaengine.rpgbase.quest;

import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;

public class Quest implements ISerializable
{
	private String m_name;
	private String m_description;

	private QuestTask[] m_tasks;

	private Quest() { }
	
	public Quest(String name, String description, QuestTask[] tasks)
	{
		m_name = name;
		m_description = description;
		m_tasks = tasks;
	}

	public QuestTask getTask(String id)
	{
		for (QuestTask t : m_tasks)
		{
			if (t.getId().compareTo(id) == 0)
				return t;
		}

		return null;
	}

	public QuestState getState()
	{
		QuestState state = QuestState.Failed;

		for (QuestTask t : m_tasks)
		{
			if (t.getState().compareTo(state) < 0)
				state = t.getState();
		}

		return state;
	}

	public String getName()
	{
		return m_name;
	}

	public String getDescription()
	{
		return m_description;
	}

	public QuestTask[] getTasks()
	{
		return m_tasks;
	}

	@Override
	public void serialize(IVariable target)
	{
		target.addChild("name").setValue(m_name);
		target.addChild("description").setValue(m_description);
		target.addChild("tasks").setValue(m_tasks);
	}

	@Override
	public void deserialize(IVariable source)
	{
		m_name = source.getChild("name").getValue(String.class);
		m_description = source.getChild("description").getValue(String.class);
		m_tasks = source.getChild("tasks").getValues(QuestTask[].class);
	}
}
