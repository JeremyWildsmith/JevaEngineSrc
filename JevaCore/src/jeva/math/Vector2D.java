/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.math;

import proguard.annotation.KeepClassMembers;
import proguard.annotation.KeepName;

/**
 * The Class Vector2D.
 * 
 * @author Scott
 */

@KeepName
@KeepClassMembers
public class Vector2D implements Comparable<Vector2D>
{

	/** The x. */
	public int x;

	/** The y. */
	public int y;

	/**
	 * Instantiates a new vector2 d.
	 * 
	 * @param _x
	 *            the _x
	 * @param _y
	 *            the _y
	 */
	public Vector2D(int _x, int _y)
	{
		x = _x;
		y = _y;
	}

	/**
	 * Instantiates a new vector2 d.
	 */
	public Vector2D()
	{
		x = 0;
		y = 0;
	}

	/**
	 * Instantiates a new vector2 d.
	 * 
	 * @param location
	 *            the location
	 */
	public Vector2D(Vector2D location)
	{
		x = location.x;
		y = location.y;
	}

	/**
	 * Adds the.
	 * 
	 * @param a
	 *            the a
	 * @return the vector2 d
	 */
	public Vector2D add(Vector2D a)
	{
		return new Vector2D(x + a.x, y + a.y);
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
	 * Checks if is zero.
	 * 
	 * @return true, if is zero
	 */
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
		if (o == null)
		{
			return false;
		}
		if (!(o instanceof Vector2D))
		{
			return false;
		}

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

	/**
	 * Difference.
	 * 
	 * @param v
	 *            the v
	 * @return the vector2 d
	 */
	public Vector2D difference(Vector2D v)
	{
		return new Vector2D(x - v.x, y - v.y);
	}
}
