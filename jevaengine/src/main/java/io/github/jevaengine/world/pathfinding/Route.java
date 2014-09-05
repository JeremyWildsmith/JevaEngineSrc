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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.world.pathfinding;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public final class Route
{
	private ArrayList<Vector2F> m_path = new ArrayList<>();

	public Route(Vector2F ... path)
	{
		m_path.addAll(Arrays.asList(path));
	}
	
	public Route() { }
	
	public Route(Route src)
	{
		m_path.addAll(src.m_path);
	}
	
	public void truncate(int maxSteps)
	{
		if (m_path.size() <= maxSteps)
			return;

		m_path = new ArrayList<Vector2F>(m_path.subList(0, maxSteps));
	}

	public int length()
	{
		return m_path.size();
	}

	@Nullable
	public Vector2F getCurrentTarget()
	{
		if (m_path.isEmpty())
			return null;

		return m_path.get(0);
	}

	public boolean nextTarget()
	{
		if (!m_path.isEmpty())
			m_path.remove(0);

		return !m_path.isEmpty();
	}

	public boolean hasNext()
	{
		return m_path.size() > 1;
	}
	
	public Vector2F peek(int ahead)
	{
		return m_path.get(ahead);
	}

	public void addWaypoint(Vector2F node)
	{
		m_path.add(node);
	}
	
	public void addWaypoints(Vector2F ... nodes)
	{
		for(Vector2F node : nodes)
			addWaypoint(node);
	}
}
