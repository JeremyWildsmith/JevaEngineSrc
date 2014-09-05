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

import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;

public final class Vector3F implements ISerializable
{
	public static final float TOLERANCE = 0.0000001F;

	public float x;
	public float y;
	public float z;

	public Vector3F(Vector3F v)
	{
		x = v.x;
		y = v.y;
		z = v.z;
	}
	
	public Vector3F(float fX, float fY, float fZ)
	{
		x = fX;
		y = fY;
		z = fZ;
	}
	
	public Vector3F(float fX, float fY)
	{
		x = fX;
		y = fY;
	}

	public Vector3F(Vector2F v, float fZ)
	{
		this(v.x, v.y, fZ);
	}
	
	public Vector3F(Vector2D v, float fZ)
	{
		this(v.x, v.y, fZ);
	}
	
	public Vector3F()
	{
		this(0, 0, 0);
	}
	
	public Vector3F add(Vector3F v)
	{
		return new Vector3F(x + v.x, y + v.y, z + v.z);
	}
	
	public Vector3D round()
	{
		return new Vector3D((int) (Math.round(Math.abs(x)) * Math.signum(x)), (int) (Math.round(Math.abs(y)) * Math.signum(y)), (int) (Math.round(Math.abs(z)) * Math.signum(z)));
	}
	
	public float getLengthSquared()
	{
		return x * x + y * y + z * z;
	}

	public float getLength()
	{
		return (float) Math.sqrt(getLengthSquared());
	}
	
	public Vector3F normalize()
	{
		float length = getLength();
		
		return new Vector3F(x / length, y / length, z / length);
	}

	public Vector3F multiply(float scale)
	{
		return new Vector3F(x * scale, y * scale, z * scale);
	}
	
	public Vector3F difference(Vector3F a)
	{
		return new Vector3F(x - a.x, y - a.y, z - a.z);
	}
	
	public boolean isZero()
	{
		return Math.abs(x) < TOLERANCE && 
				Math.abs(y) < TOLERANCE && 
				Math.abs(z) < TOLERANCE;
	}

	public Vector2F getXy()
	{
		return new Vector2F(x, y);
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		else if (o == null)
			return false;
		else if (o instanceof Vector3F)
		{
			Vector3F v = (Vector3F) o;
			
			if (Math.abs(v.z - z) > TOLERANCE)
				return false;
			else if (Math.abs(v.x - x) > TOLERANCE)
				return false;
			else if (Math.abs(v.y - y) > TOLERANCE)
				return false;
			else
				return true;
		} else
			return false;
	}

	
	@Override
	public void serialize(IVariable target) throws ValueSerializationException
	{
		target.addChild("x").setValue(x);
		target.addChild("y").setValue(y);
		target.addChild("z").setValue(z);
	}

	@Override
	public void deserialize(IImmutableVariable source) throws ValueSerializationException
	{
		try
		{
			x = source.getChild("x").getValue(Double.class).floatValue();
			y = source.getChild("y").getValue(Double.class).floatValue();
			z = source.getChild("z").getValue(Double.class).floatValue();
		} catch(NoSuchChildVariableException e)
		{
			throw new ValueSerializationException(e);
		}
	}
}
