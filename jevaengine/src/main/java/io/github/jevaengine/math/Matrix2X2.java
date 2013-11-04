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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.math;

public class Matrix2X2
{

	public float[][] matrix;

	public static final Matrix2X2 IDENTITY = new Matrix2X2(1, 0, 0, 1);

	public Matrix2X2()
	{
		matrix = new float[][]
		{
				{ 1, 0 },
				{ 0, 1 } };
	}

	public Matrix2X2(float x0y0, float x1y0, float x0y1, float x1y1)
	{
		matrix = new float[][]
		{
				{ x0y0, x0y1 },
				{ x1y0, x1y1 } };
	}

	public static Matrix2X2 createRotation(float fRot)
	{
		return new Matrix2X2((float) Math.cos(fRot), (float) -Math.sin(fRot), (float) Math.sin(fRot), (float) Math.cos(fRot));
	}

	public Vector2F dot(Vector2F vec)
	{
		return new Vector2F(vec.x * matrix[0][0] + vec.y * matrix[1][0], vec.x * matrix[0][1] + vec.y * matrix[1][1]);
	}

	public Vector2F dot(Vector2D vec)
	{
		return this.dot(new Vector2F(vec.x, vec.y));
	}

	public Matrix2X2 scale(float fScale)
	{
		return new Matrix2X2(matrix[0][0] * fScale, matrix[1][0] * fScale, matrix[0][1] * fScale, matrix[1][1] * fScale);
	}

	public float determinant()
	{
		return matrix[0][0] * matrix[1][1] - matrix[1][0] * matrix[0][1];
	}

	public Matrix2X2 inverse()
	{
		return new Matrix2X2(matrix[1][1], -matrix[1][0], -matrix[0][1], matrix[0][0]).scale(1 / (matrix[0][0] * matrix[1][1] - matrix[1][0] * matrix[0][1]));
	}
}
