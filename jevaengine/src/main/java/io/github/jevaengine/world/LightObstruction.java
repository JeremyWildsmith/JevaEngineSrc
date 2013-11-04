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

import java.awt.geom.Area;

import io.github.jevaengine.math.Vector2F;

public class LightObstruction
{

	private Area m_area;

	private Vector2F m_direction;

	private float m_fVisibilityFactor;

	public LightObstruction(Area area, Vector2F direction, float fVisibilityFactor)
	{
		m_area = area;
		m_direction = (direction.isZero() ? direction : direction.normalize());
		m_fVisibilityFactor = fVisibilityFactor;
	}

	public Area getArea()
	{
		return m_area;
	}

	public Vector2F getDirection()
	{
		return m_direction;
	}

	public float getVisibilityFactor()
	{
		return m_fVisibilityFactor;
	}
}
