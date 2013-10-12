/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.math;

import proguard.annotation.KeepClassMembers;
import proguard.annotation.KeepName;

/**
 * The Class Vector2F.
 * 
 * @author Scott
 */

@KeepName
@KeepClassMembers
public class Vector2F implements Comparable<Vector2F>
{

	/** The Constant TOLERANCE. */
	public static final float TOLERANCE = 0.00000001F;

	/** The x. */
	public float x;

	/** The y. */
	public float y;

	/**
	 * Instantiates a new vector2 f.
	 * 
	 * @param v
	 *            the v
	 */
	public Vector2F(Vector2F v)
	{
		x = v.x;
		y = v.y;
	}

	/**
	 * Instantiates a new vector2 f.
	 * 
	 * @param v
	 *            the v
	 */
	public Vector2F(Vector2D v)
	{
		x = v.x;
		y = v.y;
	}

	/**
	 * Instantiates a new vector2 f.
	 * 
	 * @param fX
	 *            the f x
	 * @param fY
	 *            the f y
	 */
	public Vector2F(float fX, float fY)
	{
		x = fX;
		y = fY;
	}

	/**
	 * Instantiates a new vector2 f.
	 */
	public Vector2F()
	{
		x = 0;
		y = 0;
	}

	/**
	 * Checks if is zero.
	 * 
	 * @return true, if is zero
	 */
	public boolean isZero()
	{
		return Math.abs(x) < TOLERANCE && Math.abs(y) < TOLERANCE;
	}

	/**
	 * Rotate.
	 * 
	 * @param origin
	 *            the origin
	 * @param fAngle
	 *            the f angle
	 * @return the vector2 f
	 */
	public Vector2F rotate(Vector2F origin, float fAngle)
	{
		Vector2F rotated = new Vector2F(this).difference(origin);

		return Matrix2X2.createRotation(fAngle).dot(rotated).add(origin);
	}

	/**
	 * Rotate.
	 * 
	 * @param fAngle
	 *            the f angle
	 * @return the vector2 f
	 */
	public Vector2F rotate(float fAngle)
	{
		return rotate(new Vector2F(), fAngle);
	}

	/**
	 * Floor.
	 * 
	 * @return the vector2 d
	 */
	public Vector2D floor()
	{
		return new Vector2D((int) Math.floor(x), (int) Math.floor(y));
	}

	/**
	 * Round.
	 * 
	 * @return the vector2 d
	 */
	public Vector2D round()
	{
		return new Vector2D((int) (Math.round(Math.abs(x)) * Math.signum(x)), (int) (Math.round(Math.abs(y)) * Math.signum(y)));
	}

	/**
	 * Negative.
	 * 
	 * @return the vector2 f
	 */
	public Vector2F negative()
	{
		return new Vector2F(-x, -y);
	}

	/**
	 * Gets the length squared.
	 * 
	 * @return the length squared
	 */
	public float getLengthSquared()
	{
		return x * x + y * y;
	}

	/**
	 * Gets the length.
	 * 
	 * @return the length
	 */
	public float getLength()
	{
		return (float) Math.sqrt(getLengthSquared());
	}

	/**
	 * Difference.
	 * 
	 * @param a
	 *            the a
	 * @return the vector2 f
	 */
	public Vector2F difference(Vector2F a)
	{
		return new Vector2F(x - a.x, y - a.y);
	}

	/**
	 * Difference.
	 * 
	 * @param a
	 *            the a
	 * @return the vector2 f
	 */
	public Vector2F difference(Vector2D a)
	{
		return difference(new Vector2F(a.x, a.y));
	}

	/**
	 * Normalize.
	 * 
	 * @return the vector2 f
	 */
	public Vector2F normalize()
	{
		return this.divide(getLength());
	}

	/**
	 * Adds the.
	 * 
	 * @param a
	 *            the a
	 * @return the vector2 f
	 */
	public Vector2F add(Vector2F a)
	{
		return new Vector2F(x + a.x, y + a.y);
	}

	/**
	 * Multiply.
	 * 
	 * @param fScale
	 *            the f scale
	 * @return the vector2 f
	 */
	public Vector2F multiply(float fScale)
	{
		return new Vector2F(x * fScale, y * fScale);
	}

	/**
	 * Divide.
	 * 
	 * @param f
	 *            the f
	 * @return the vector2 f
	 */
	public Vector2F divide(float f)
	{
		return new Vector2F(x / f, y / f);
	}

	/**
	 * Min.
	 * 
	 * @param a
	 *            the a
	 * @param b
	 *            the b
	 * @return the vector2 f
	 */
	public static Vector2F min(Vector2F a, Vector2F b)
	{
		return (a.getLengthSquared() > b.getLengthSquared() ? b : a);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		} else if (o instanceof Vector2F)
		{
			Vector2F vec = (Vector2F) o;
			return compareTo(vec) == 0;
		} else
		{
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 41 * hash + Float.floatToIntBits(this.x);
		hash = 41 * hash + Float.floatToIntBits(this.y);
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Vector2F v)
	{
		if (Math.abs(v.x - x) < TOLERANCE && Math.abs(v.y - y) < TOLERANCE)
			return 0;

		if (Math.abs(v.y - y) < TOLERANCE)
			return (x < v.x ? -1 : 1);
		else if (y < v.y)
			return -1;
		else
			return 1;
	}
}
