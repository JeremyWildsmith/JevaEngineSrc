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
package io.github.jevaengine.math;

import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;

public class Vector2D implements Comparable<Vector2D>, ISerializable
{

	public int x;

	public int y;

	public Vector2D(int _x, int _y)
	{
		x = _x;
		y = _y;
	}

	public Vector2D()
	{
		x = 0;
		y = 0;
	}

	public Vector2D(Vector2D location)
	{
		x = location.x;
		y = location.y;
	}

	public Vector2D add(Vector2D a)
	{
		return new Vector2D(x + a.x, y + a.y);
	}

	public float getLengthSquared()
	{
		return x * x + y * y;
	}

	public float getAngle()
	{
		return (float)Math.atan2(y, x);
	}
	
	public float getLength()
	{
		return (float) Math.sqrt(getLengthSquared());
	}

	public boolean isZero()
	{
		return (x == 0 && y == 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		else if (o == null)
			return false;
		else if (!(o instanceof Vector2D))
			return false;

		Vector2D vec = (Vector2D) o;

		return (vec.x == x && vec.y == y);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 83 * hash + this.x;
		hash = 83 * hash + this.y;
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Vector2D v)
	{
		if (v.x == x && v.y == y)
			return 0;

		if (y == v.y)
			return (x < v.x ? -1 : 1);
		else if (y < v.y)
			return -1;
		else
			return 1;
	}

	public Vector2D difference(Vector2D v)
	{
		return new Vector2D(x - v.x, y - v.y);
	}
	
		@Override
	public void serialize(IVariable target)
	{
		target.addChild("x").setValue(x);
		target.addChild("y").setValue(y);
	}

	@Override
	public void deserialize(IVariable source)
	{
		x = source.getChild("x").getValue(Integer.class);
		y = source.getChild("y").getValue(Integer.class);
	}
}
