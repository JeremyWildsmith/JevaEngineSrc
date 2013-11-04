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
package io.github.jevaengine.mapeditor;

import io.github.jevaengine.config.VariableValue;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class EditorEntity
{
	private Entity m_containedEntity;

	private String m_name;
	private String m_className;
	private String m_arguments;

	private HashMap<String, String> m_postInitAssignment;

	public EditorEntity(String name, String className, String arguments)
	{
		m_name = name;
		m_className = className;
		m_arguments = arguments;

		m_postInitAssignment = new HashMap<String, String>();
		;
	}

	public void setName(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public void setClassName(String className)
	{
		m_className = className;
	}

	public String getClassName()
	{
		return m_className;
	}

	public String getArguments()
	{
		return m_arguments;
	}

	public void setArguments(String arguments)
	{
		m_arguments = arguments;
	}

	public void setPostInitAssignment(String name, String value)
	{
		m_postInitAssignment.put(name, value);
	}

	public void removePostInitAssignment(String name)
	{
		m_postInitAssignment.remove(name);
	}

	public void clearPostInitAssignments()
	{
		m_postInitAssignment.clear();
	}

	public Set<Entry<String, String>> getPostInitAssignments()
	{
		return m_postInitAssignment.entrySet();
	}

	public String getPostInitAssignment(String name)
	{
		return m_postInitAssignment.get(name);
	}

	public void refresh(World world)
	{
		if (m_containedEntity != null)
			world.removeEntity(m_containedEntity);

		m_containedEntity = world.getEntityLibrary().createEntity(m_className, m_name, Arrays.asList((new VariableValue(m_arguments)).getObjectArguments()));

		for (Map.Entry<String, String> assignment : m_postInitAssignment.entrySet())
			m_containedEntity.setVariable(assignment.getKey(), new VariableValue(assignment.getValue()));

		m_containedEntity.pause();

		world.addEntity(m_containedEntity);
	}

	public void remove(World world)
	{
		if (m_containedEntity != null)
			world.removeEntity(m_containedEntity);
	}

	@Override
	public String toString()
	{
		return String.format("%s of %s with %s", m_name, m_className, m_arguments);
	}
}
