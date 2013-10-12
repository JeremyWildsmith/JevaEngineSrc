package UnitTest;

public class Vector3F
{
	public static final float TOLERANCE = 0.0000001F;

	public float x;
	public float y;
	public float z;

	public Vector3F(Vector2F v, float fZ)
	{
		x = v.x;
		y = v.y;
		z = fZ;
	}

	public Vector3F(Vector2D v, float fZ)
	{
		x = v.x;
		y = v.y;
		z = fZ;
	}

	public Vector3F(float fX, float fY, float fZ)
	{
		x = fX;
		y = fY;
		z = fZ;
	}
}
