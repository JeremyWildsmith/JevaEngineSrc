/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.math;

/**
 * The Class Matrix2X2.
 * 
 * @author Scott
 */
public class Matrix2X2
{

	/** The matrix. */
	public float[][] matrix;

	/** The Constant IDENTITY. */
	public static final Matrix2X2 IDENTITY = new Matrix2X2(1, 0, 0, 1);

	/**
	 * Instantiates a new matrix2 x2.
	 */
	public Matrix2X2()
	{
		matrix = new float[][]
		{
		{ 1, 0 },
		{ 0, 1 } };
	}

	/**
	 * Instantiates a new matrix2 x2.
	 * 
	 * @param x0y0
	 *            the x0y0
	 * @param x1y0
	 *            the x1y0
	 * @param x0y1
	 *            the x0y1
	 * @param x1y1
	 *            the x1y1
	 */
	public Matrix2X2(float x0y0, float x1y0, float x0y1, float x1y1)
	{
		matrix = new float[][]
		{
		{ x0y0, x0y1 },
		{ x1y0, x1y1 } };
	}

	/**
	 * Creates the rotation.
	 * 
	 * @param fRot
	 *            the f rot
	 * @return the matrix2 x2
	 */
	public static Matrix2X2 createRotation(float fRot)
	{
		return new Matrix2X2((float) Math.cos(fRot), (float) -Math.sin(fRot), (float) Math.sin(fRot), (float) Math.cos(fRot));
	}

	/**
	 * Dot.
	 * 
	 * @param vec
	 *            the vec
	 * @return the vector2 f
	 */
	public Vector2F dot(Vector2F vec)
	{
		return new Vector2F(vec.x * matrix[0][0] + vec.y * matrix[1][0], vec.x * matrix[0][1] + vec.y * matrix[1][1]);
	}

	/**
	 * Dot.
	 * 
	 * @param vec
	 *            the vec
	 * @return the vector2 f
	 */
	public Vector2F dot(Vector2D vec)
	{
		return this.dot(new Vector2F(vec.x, vec.y));
	}

	/**
	 * Scale.
	 * 
	 * @param fScale
	 *            the f scale
	 * @return the matrix2 x2
	 */
	public Matrix2X2 scale(float fScale)
	{
		return new Matrix2X2(matrix[0][0] * fScale, matrix[1][0] * fScale, matrix[0][1] * fScale, matrix[1][1] * fScale);
	}

	/**
	 * Determinant.
	 * 
	 * @return the float
	 */
	public float determinant()
	{
		return matrix[0][0] * matrix[1][1] - matrix[1][0] * matrix[0][1];
	}

	/**
	 * Inverse.
	 * 
	 * @return the matrix2 x2
	 */
	public Matrix2X2 inverse()
	{
		return new Matrix2X2(matrix[1][1], -matrix[1][0], -matrix[0][1], matrix[0][0]).scale(1 / (matrix[0][0] * matrix[1][1] - matrix[1][0] * matrix[0][1]));
	}
}
