package UnitTest;

public class Matrix3X3
{

	public float[][] matrix;

	public static final Matrix2X2 IDENTITY = new Matrix2X2(1, 0, 0, 1);

	public Matrix3X3(float x0y0, float x1y0, float x2y0, float x0y1, float x1y1, float x2y1, float x0y2, float x1y2, float x2y2)
	{
		matrix = new float[][]
		{
		{ x0y0, x0y1, x0y2 },
		{ x1y0, x1y1, x1y2 },
		{ x2y0, x2y1, x2y2 } };
	}

	public Matrix3X3(Matrix3X3 mat)
	{
		matrix = mat.matrix;
	}

	public Vector3F dot(Vector3F v)
	{
		return new Vector3F(v.x * matrix[0][0] + v.y * matrix[1][0] + v.z * matrix[2][0], v.x * matrix[0][1] + v.y * matrix[1][1] + v.z * matrix[2][1], v.x * matrix[0][2] + v.y * matrix[1][2] + v.z * matrix[2][2]);
	}

	public Matrix3X3 scale(float fScale)
	{
		return new Matrix3X3(matrix[0][0] * fScale, matrix[1][0] * fScale, matrix[2][0] * fScale, matrix[0][1] * fScale, matrix[1][1] * fScale, matrix[2][1] * fScale, matrix[0][2] * fScale, matrix[1][2] * fScale, matrix[2][2] * fScale);
	}

	public Matrix3X3 transpose()
	{
		return new Matrix3X3(matrix[0][0], matrix[0][1], matrix[0][2], matrix[1][0], matrix[1][1], matrix[1][2], matrix[2][0], matrix[2][1], matrix[2][2]);
	}

	public Matrix3X3 adjoint()
	{
		float[][] m = new Matrix3X3(this).transpose().matrix;

		return new Matrix3X3(new Matrix2X2(m[1][1], m[2][1], m[1][2], m[2][2]).determinant(), -new Matrix2X2(m[0][1], m[2][1], m[0][2], m[2][2]).determinant(), new Matrix2X2(m[0][1], m[1][1], m[0][2], m[1][2]).determinant(), -new Matrix2X2(m[1][0], m[2][0], m[1][2], m[2][2]).determinant(), new Matrix2X2(m[0][0], m[2][0], m[0][2], m[2][2]).determinant(), -new Matrix2X2(m[0][0], m[1][0], m[0][2], m[1][2]).determinant(), new Matrix2X2(m[1][0], m[2][0], m[1][1], m[2][1]).determinant(), -new Matrix2X2(m[0][0], m[2][0], m[0][1], m[2][1]).determinant(), new Matrix2X2(m[0][0], m[1][0], m[0][1], m[1][1]).determinant());
	}

	public float determinant()
	{
		float[][] m = new Matrix3X3(this).transpose().matrix;

		return m[0][0] * new Matrix2X2(m[1][1], m[2][1], m[1][2], m[2][2]).determinant() - m[1][0] * new Matrix2X2(m[0][1], m[2][1], m[0][2], m[2][2]).determinant() + m[2][0] * new Matrix2X2(m[0][1], m[1][1], m[0][2], m[1][2]).determinant();
	}

	public Matrix3X3 inverse()
	{
		return this.adjoint().scale(1 / determinant());
	}
}
