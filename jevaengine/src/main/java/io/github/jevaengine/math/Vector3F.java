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
package io.github.jevaengine.math;

public class Vector3F implements Comparable<Vector3F>
{
	public static final float TOLERANCE = 0.0000001F;

	private SortingModel m_sortingModel;
	
	public float x;
	public float y;
	public float z;

	public Vector3F(float fX, float fY, float fZ)
	{
		x = fX;
		y = fY;
		z = fZ;
		
		m_sortingModel = SortingModel.Distance;
	}
	
	public Vector3F(float fX, float fY, float fZ, SortingModel sortingModel)
	{
		this(fX, fY, fZ);
		m_sortingModel = sortingModel;
	}
	
	public Vector3F(Vector2F v, float fZ)
	{
		this(v.x, v.y, fZ);
		m_sortingModel = v.getSortingModel();
	}
	
	public SortingModel getSortingModel()
	{
		return m_sortingModel;
	}

	@Override
	public int compareTo(Vector3F v)
	{
		if (Math.abs(v.z - z) > TOLERANCE)
			return (z < v.z ? -1 : 1);

		SortingModel model = SortingModel.values()[Math.max(m_sortingModel.ordinal(), v.getSortingModel().ordinal())];
		
		if(model == SortingModel.Distance)
		{
			float distanceDifference = x * x + y * y - (v.x * v.x + v.y * v.y);
			
			if(Math.abs(distanceDifference) > TOLERANCE)
			{
				//If there is a difference in x, and their signs are not equal (i.e, in different quadrants)
				if(Math.abs(v.x - x) > TOLERANCE && (x < 0) != (v.x < 0))
					return x < v.x ? -1 : 1;
				else if(Math.abs(v.y - y) > TOLERANCE && (y < 0) != (v.y < 0))
					return y < v.y ? -1 : 1;
				else
					return distanceDifference > 0 ? 1 : -1;
			}else if (Math.abs(v.z - z) > TOLERANCE)
				return (z < v.z ? -1 : 1);
			else if (Math.abs(v.x - x) > TOLERANCE)
				return (x < v.x ? -1 : 1);
			else if (Math.abs(v.y - y) > TOLERANCE)
				return (y < v.y ? -1 : 1);
			else
				return 0;
			
		}else if(model == SortingModel.XOnly)
		{
			if (Math.abs(v.x - x) > TOLERANCE)
				return (x < v.x ? -1 : 1);
			else
				return 0;
		}else if(model == SortingModel.YOnly)
		{
			if (Math.abs(v.y - y) > TOLERANCE)
				return (y < v.y ? -1 : 1);
			else
				return 0;
		}else
			throw new UnsupportedOperationException();
	}
}
