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

import io.github.jevaengine.ResourceFormatException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;

public class Vector2D implements Comparable<Vector2D>, ISerializable
{
	private SortingModel m_sortingModel;
	
	public int x;
	public int y;

	public Vector2D(int _x, int _y, SortingModel model)
	{
		x = _x;
		y = _y;
		m_sortingModel = model;
	}
	

	public Vector2D(int _x, int _y)
	{
		this(_x, _y, SortingModel.Distance);
	}
	
	public Vector2D(Vector2D location)
	{
		this(location.x, location.y, location.getSortingModel());
	}
	
	public Vector2D()
	{
		this(0,0);
	}
	
	public SortingModel getSortingModel()
	{
		return m_sortingModel;
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

	@Override
	public int compareTo(Vector2D v)
	{
		if(m_sortingModel == SortingModel.Distance)
		{
			int distanceDifference = x * x + y * y - (v.x * v.x + v.y * v.y);
			
			if(distanceDifference > 0)
			{
				//If there is a difference in x, and their signs are not equal (i.e, in different quadrants)
				if(v.x != x && (x < 0) != (v.x < 0))
					return x < v.x ? -1 : 1;
				else if(v.y != y && (y < 0) != (v.y < 0))
					return y < v.y ? -1 : 1;
				else
					return distanceDifference > 0 ? 1 : -1;
			}else if (v.x != x)
				return (x < v.x ? -1 : 1);
			else if (v.y != y)
				return (y < v.y ? -1 : 1);
			else
				return 0;
			
		}else if(m_sortingModel == SortingModel.XOnly)
		{
			if (v.x != x)
				return (x < v.x ? -1 : 1);
			else
				return 0;
		}else if(m_sortingModel == SortingModel.YOnly)
		{
			if (v.y != y)
				return (y < v.y ? -1 : 1);
			else
				return 0;
		}else
			throw new UnsupportedOperationException();
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
		
		//If not default
		if(m_sortingModel != SortingModel.Distance)
			target.addChild("sorting").setValue(m_sortingModel.ordinal());
	}

	@Override
	public void deserialize(IImmutableVariable source)
	{
		x = source.getChild("x").getValue(Integer.class);
		y = source.getChild("y").getValue(Integer.class);
		
		if(source.childExists("sorting"))
		{
			int sortingBuffer = source.getChild("sorting").getValue(Integer.class);
			
			if(sortingBuffer < 0 || sortingBuffer >= SortingModel.values().length)
				throw new ResourceFormatException("Sorting model index is invalid.");
			
			m_sortingModel = SortingModel.values()[sortingBuffer];
		}else
			m_sortingModel = SortingModel.Distance;
	}
}
