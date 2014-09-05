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
package io.github.jevaengine.world;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;

public enum Direction
{
	XPlus(new Vector2D(1, 0), false),
	YPlus(new Vector2D(0, 1), false),
	XMinus(new Vector2D(-1, 0), false),
	YMinus(new Vector2D(0, -1), false),
	XYPlus(new Vector2D(1, 1), true),
	XYMinus(new Vector2D(-1, -1), true),
	XYPlusMinus(new Vector2D(1, -1), true),
	XYMinusPlus(new Vector2D(-1, 1), true),
	Zero(new Vector2D(0, 0), false);

	public static final Direction[] HV_DIRECTIONS =
	{ XPlus, YPlus, XMinus, YMinus };

	public static final Direction[] DIAGONAL_DIRECTIONS =
	{ XYPlus, XYMinus, XYPlusMinus, XYMinusPlus };

	public static final Direction[] ALL_DIRECTIONS =
	{ XPlus, YPlus, XMinus, YMinus, XYPlus, XYMinus, XYPlusMinus, XYMinusPlus };

	private Vector2D m_movementVector;

	private boolean m_isDiagonal;

	Direction(Vector2D movementVector, boolean isDiagonal)
	{
		m_movementVector = movementVector;
		m_isDiagonal = isDiagonal;
	}

	public boolean isDiagonal()
	{
		return m_isDiagonal;
	}

	public static Direction fromAngle(float angle)
	{
		Vector2F vec = new Vector2F(1, 0);
		
		return fromVector(vec.rotate(angle));
	}
	
	public static Direction fromVector(Vector2F vec)
	{
		if (vec.isZero())
			return Direction.Zero;

		Vector2F dir = vec.normalize();
		float fAngle = (float) Math.atan(Math.abs(dir.y) / Math.abs(dir.x));

		if (fAngle < Math.PI / 4 - Math.PI / 5.5 || fAngle > Math.PI / 4 + Math.PI / 5.5)
		{
			if (dir.x > Vector2F.TOLERANCE && dir.y > Vector2F.TOLERANCE)
				return dir.x > dir.y ? XPlus : YPlus;
			else if (dir.x < -Vector2F.TOLERANCE && dir.y < -Vector2F.TOLERANCE)
				return (-dir.x > -dir.y ? XMinus : YMinus);
			else if (dir.y > Vector2F.TOLERANCE && dir.x < -Vector2F.TOLERANCE)
				return (dir.y > -dir.x ? YPlus : XMinus);
			else if (dir.y < -Vector2F.TOLERANCE && dir.x > Vector2F.TOLERANCE)
				return (-dir.y > dir.x ? YMinus : XPlus);
			else if (Math.abs(dir.x) >= Vector2F.TOLERANCE && Math.abs(dir.y) <= Vector2F.TOLERANCE)
				return (dir.x < 0 ? XMinus : XPlus);
			else if (Math.abs(dir.x) <= Vector2F.TOLERANCE && Math.abs(dir.y) >= Vector2F.TOLERANCE)
				return (dir.y > 0 ? YPlus : YMinus);
		} else
		{
			if (vec.x > Vector2F.TOLERANCE && vec.y > Vector2F.TOLERANCE)
				return XYPlus;
			else if (vec.x > Vector2F.TOLERANCE && vec.y < -Vector2F.TOLERANCE)
				return XYPlusMinus;
			else if (vec.x < -Vector2F.TOLERANCE && vec.y > Vector2F.TOLERANCE)
				return XYMinusPlus;
			else if (vec.x < -Vector2F.TOLERANCE && vec.y < -Vector2F.TOLERANCE)
				return XYMinus;
		}

		return Direction.Zero;
	}

	public Vector2D getDirectionVector()
	{
		return m_movementVector;
	}

	public float getAngle()
	{
		switch (this)
		{
			case XMinus:
				return (float) Math.PI;
			case XPlus:
				return 0;
			case XYMinus:
				return 3 * (float) Math.PI / 4.0F;
			case XYMinusPlus:
				return 5 * (float) Math.PI / 4.0F;
			case XYPlus:
				return 7 * (float) Math.PI / 4.0F;
			case XYPlusMinus:
				return (float) Math.PI / 4.0F;
			case YMinus:
				return (float) Math.PI / 2.0F;
			case YPlus:
				return 3 * (float) Math.PI / 2.0F;
			case Zero:
				return 0;
			default:
				return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	public String toString()
	{
		switch (this)
		{
			case XMinus:
				return "w";
			case XPlus:
				return "e";
			case XYMinus:
				return "nw";
			case XYMinusPlus:
				return "sw";
			case XYPlus:
				return "se";
			case XYPlusMinus:
				return "ne";
			case YMinus:
				return "n";
			case YPlus:
				return "s";
			case Zero:
				return "z";
			default:
				throw new RuntimeException("Unknown world direction");
		}
	}
}
