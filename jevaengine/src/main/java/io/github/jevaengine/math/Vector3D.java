package io.github.jevaengine.math;

public class Vector3D
{
	public int x;

	public int y;

	public int z;

	public Vector3D(Vector2D _v, int _z)
	{
		x = _v.x;
		y = _v.y;
		z = _z;
	}

	public Vector3D(int _x, int _y, int _z)
	{
		x = _x;
		y = _y;
		z = _z;
	}
}
