package io.github.jevaengine.math;

public class Vector4F
{
	public static final float TOLERANCE = 0.0000001F;

	public float x;
	public float y;
	public float z;
	public float w;

	public Vector4F(float _x, float _y, float _z, float _w)
	{
		x = _x;
		y = _y;
		z = _z;
		w = _w;
	}
}
