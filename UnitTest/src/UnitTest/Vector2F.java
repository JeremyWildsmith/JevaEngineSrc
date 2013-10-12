/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UnitTest;

/**
 * 
 * @author Scott
 */
public class Vector2F implements Comparable<Vector2F>
{
	public static final float TOLERANCE = 0.0000001F;

	public float x;
	public float y;

	public Vector2F(Vector2F v)
	{
		x = v.x;
		y = v.y;
	}

	public Vector2F(Vector2D v)
	{
		x = v.x;
		y = v.y;
	}

	public Vector2F(float fX, float fY)
	{
		x = fX;
		y = fY;
	}

	public Vector2F()
	{
		x = 0;
		y = 0;
	}

	public boolean isZero()
	{
		return Math.abs(x) < TOLERANCE && Math.abs(y) < TOLERANCE;
	}

	public Vector2F rotate(Vector2F origin, float fAngle)
	{
		Vector2F rotated = new Vector2F(this).difference(origin);

		return Matrix2X2.createRotation(fAngle).dot(rotated).add(origin);
	}

	public Vector2F rotate(float fAngle)
	{
		return rotate(new Vector2F(), fAngle);
	}

	public Vector2D floor()
	{
		return new Vector2D((int) Math.floor(x), (int) Math.floor(y));
	}

	public Vector2D round()
	{
		return new Vector2D((int) (Math.round(Math.abs(x)) * Math.signum(x)), (int) (Math.round(Math.abs(y)) * Math.signum(y)));
	}

	public Vector2F negative()
	{
		return new Vector2F(-x, -y);
	}

	public float getLengthSquared()
	{
		return x * x + y * y;
	}

	public float getLength()
	{
		return (float) Math.sqrt(getLengthSquared());
	}

	public Vector2F difference(Vector2F a)
	{
		return new Vector2F(x - a.x, y - a.y);
	}

	public Vector2F difference(Vector2D a)
	{
		return difference(new Vector2F(a.x, a.y));
	}

	public Vector2F normalize()
	{
		return this.divide(getLength());
	}

	public Vector2F add(Vector2F a)
	{
		return new Vector2F(x + a.x, y + a.y);
	}

	public Vector2F multiply(float fScale)
	{
		return new Vector2F(x * fScale, y * fScale);
	}

	public Vector2F divide(float f)
	{
		return new Vector2F(x / f, y / f);
	}

	public static Vector2F min(Vector2F a, Vector2F b)
	{
		return (a.getLengthSquared() > b.getLengthSquared() ? b : a);
	}

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

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 41 * hash + Float.floatToIntBits(this.x);
		hash = 41 * hash + Float.floatToIntBits(this.y);
		return hash;
	}

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
