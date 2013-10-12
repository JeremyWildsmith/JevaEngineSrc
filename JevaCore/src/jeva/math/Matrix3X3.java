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
package jeva.math;

/**
 * The Class Matrix3X3.
 */
public class Matrix3X3
{

	/** The matrix. */
	public float[][] matrix;

	/** The Constant IDENTITY. */
	public static final Matrix2X2 IDENTITY = new Matrix2X2(1, 0, 0, 1);

	/**
	 * Instantiates a new matrix3 x3.
	 * 
	 * @param x0y0
	 *            the x0y0
	 * @param x1y0
	 *            the x1y0
	 * @param x2y0
	 *            the x2y0
	 * @param x0y1
	 *            the x0y1
	 * @param x1y1
	 *            the x1y1
	 * @param x2y1
	 *            the x2y1
	 * @param x0y2
	 *            the x0y2
	 * @param x1y2
	 *            the x1y2
	 * @param x2y2
	 *            the x2y2
	 */
	public Matrix3X3(float x0y0, float x1y0, float x2y0, float x0y1, float x1y1, float x2y1, float x0y2, float x1y2, float x2y2)
	{
		matrix = new float[][]
		{
		{ x0y0, x0y1, x0y2 },
		{ x1y0, x1y1, x1y2 },
		{ x2y0, x2y1, x2y2 } };
	}

	/**
	 * Instantiates a new matrix3 x3.
	 * 
	 * @param mat
	 *            the mat
	 */
	public Matrix3X3(Matrix3X3 mat)
	{
		matrix = mat.matrix;
	}

	/**
	 * Dot.
	 * 
	 * @param v
	 *            the v
	 * @return the vector3 f
	 */
	public Vector3F dot(Vector3F v)
	{
		return new Vector3F(v.x * matrix[0][0] + v.y * matrix[1][0] + v.z * matrix[2][0], v.x * matrix[0][1] + v.y * matrix[1][1] + v.z * matrix[2][1], v.x * matrix[0][2] + v.y * matrix[1][2] + v.z * matrix[2][2]);
	}

	/**
	 * Scale.
	 * 
	 * @param fScale
	 *            the f scale
	 * @return the matrix3 x3
	 */
	public Matrix3X3 scale(float fScale)
	{
		return new Matrix3X3(matrix[0][0] * fScale, matrix[1][0] * fScale, matrix[2][0] * fScale, matrix[0][1] * fScale, matrix[1][1] * fScale, matrix[2][1] * fScale, matrix[0][2] * fScale, matrix[1][2] * fScale, matrix[2][2] * fScale);
	}

	/**
	 * Transpose.
	 * 
	 * @return the matrix3 x3
	 */
	public Matrix3X3 transpose()
	{
		return new Matrix3X3(matrix[0][0], matrix[0][1], matrix[0][2], matrix[1][0], matrix[1][1], matrix[1][2], matrix[2][0], matrix[2][1], matrix[2][2]);
	}

	/**
	 * Adjoint.
	 * 
	 * @return the matrix3 x3
	 */
	public Matrix3X3 adjoint()
	{
		float[][] m = new Matrix3X3(this).transpose().matrix;

		return new Matrix3X3(new Matrix2X2(m[1][1], m[2][1], m[1][2], m[2][2]).determinant(), -new Matrix2X2(m[0][1], m[2][1], m[0][2], m[2][2]).determinant(), new Matrix2X2(m[0][1], m[1][1], m[0][2], m[1][2]).determinant(), -new Matrix2X2(m[1][0], m[2][0], m[1][2], m[2][2]).determinant(), new Matrix2X2(m[0][0], m[2][0], m[0][2], m[2][2]).determinant(), -new Matrix2X2(m[0][0], m[1][0], m[0][2], m[1][2]).determinant(), new Matrix2X2(m[1][0], m[2][0], m[1][1], m[2][1]).determinant(), -new Matrix2X2(m[0][0], m[2][0], m[0][1], m[2][1]).determinant(), new Matrix2X2(m[0][0], m[1][0], m[0][1], m[1][1]).determinant());
	}

	/**
	 * Determinant.
	 * 
	 * @return the float
	 */
	public float determinant()
	{
		float[][] m = new Matrix3X3(this).transpose().matrix;

		return m[0][0] * new Matrix2X2(m[1][1], m[2][1], m[1][2], m[2][2]).determinant() - m[1][0] * new Matrix2X2(m[0][1], m[2][1], m[0][2], m[2][2]).determinant() + m[2][0] * new Matrix2X2(m[0][1], m[1][1], m[0][2], m[1][2]).determinant();
	}

	/**
	 * Inverse.
	 * 
	 * @return the matrix3 x3
	 */
	public Matrix3X3 inverse()
	{
		return this.adjoint().scale(1 / determinant());
	}
}
