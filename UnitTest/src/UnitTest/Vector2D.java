/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitTest;

/**
 * 
 * @author Scott
 */
public class Vector2D implements Comparable<Vector2D>
{

	public int x;
	public int y;

	public Vector2D(int x, int y)
	{
		x = x;
		y = y;
	}

	public Vector2D()
	{
		x = 0;
		y = 0;
	}

	public Vector2D add(Vector2D a)
	{
		return new Vector2D(x + a.x, y + a.y);
	}

	public boolean isZero()
	{
		return (x == 0 && y == 0);
	}

	public float getLengthSquared()
	{
		return x * x + y * y;
	}

	public float getLength()
	{
		return (float) Math.sqrt(getLengthSquared());
	}

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
}
