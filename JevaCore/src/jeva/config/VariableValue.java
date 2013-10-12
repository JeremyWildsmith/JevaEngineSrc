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
package jeva.config;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Class VariableValue.
 * 
 * @author Scott
 */
public final class VariableValue
{

	/** The Constant REGEX_OBJECTARGS. */
	private static final Pattern REGEX_OBJECTARGS = Pattern.compile("(?<class>[^(]*)\\((?<arguments>[^)]*)\\)");

	/** The Constant REGEX_ARGUMENT. */
	private static final Pattern REGEX_ARGUMENT = Pattern.compile(",?\\s*\"(?<argument>[^\\\"]*)\"");

	/** The Constant LIST_SPLIT. */
	private static final String LIST_SPLIT = ",";

	/** The m_value. */
	private String m_value;

	/**
	 * Instantiates a new variable value.
	 */
	public VariableValue()
	{
		m_value = "";
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param value
	 *            the value
	 */
	public VariableValue(String value)
	{
		m_value = value;
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param value
	 *            the value
	 */
	public VariableValue(int value)
	{
		m_value = String.valueOf(value);
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param value
	 *            the value
	 */
	public VariableValue(boolean value)
	{
		m_value = String.valueOf(value);
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param fValue
	 *            the f value
	 */
	public VariableValue(float fValue)
	{
		m_value = String.valueOf(fValue);
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param rect
	 *            the rect
	 */
	public VariableValue(Rectangle rect)
	{
		m_value = String.format("%d,%d,%d,%d", rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param point
	 *            the point
	 */
	public VariableValue(Point point)
	{
		m_value = String.format("%d,%d", point.x, point.y);
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param object
	 *            the object
	 * @param arguments
	 *            the arguments
	 */
	public VariableValue(String object, String... arguments)
	{
		String formattedArgumentList = "";

		for (String arg : arguments)
		{
			formattedArgumentList += String.format("\"%s\"", (formattedArgumentList.isEmpty() ? "" : ",") + arg);
		}

		m_value = String.format("%s(%s)", object, formattedArgumentList);
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param argList
	 *            the arg list
	 */
	public VariableValue(VariableValue... argList)
	{
		StringBuilder sb = new StringBuilder("(");

		for (int i = 0; i < argList.length; i++)
		{
			sb.append("\"" + argList[i].getString() + "\"" + (i >= argList.length - 1 ? "" : ","));
		}
		sb.append(")");

		m_value = sb.toString();
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param argList
	 *            the arg list
	 */
	public VariableValue(int... argList)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < argList.length; i++)
		{
			sb.append(String.valueOf(argList[i]) + (i >= argList.length - 1 ? "" : ","));
		}

		m_value = sb.toString();
	}

	/**
	 * Instantiates a new variable value.
	 * 
	 * @param argList
	 *            the arg list
	 */
	public VariableValue(Integer... argList)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < argList.length; i++)
		{
			sb.append(String.valueOf(argList[i]) + (i >= argList.length - 1 ? "" : ","));
		}

		m_value = sb.toString();
	}

	/**
	 * Gets the value no whitespace.
	 * 
	 * @return the value no whitespace
	 */
	private String getValueNoWhitespace()
	{
		return m_value.replaceAll("\\s", "");
	}

	/**
	 * Gets the point.
	 * 
	 * @return the point
	 */
	public Point getPoint()
	{
		Integer[] point = getIntArray();

		if (point.length != 2)
		{
			throw new UnknownVariableValueFormException();
		}

		return new Point(point[0], point[1]);
	}

	/**
	 * Gets the rectangle.
	 * 
	 * @return the rectangle
	 */
	public Rectangle getRectangle()
	{
		Integer[] rect = getIntArray();

		if (rect.length != 4)
		{
			throw new UnknownVariableValueFormException();
		}

		return new Rectangle(rect[0], rect[1], rect[2], rect[3]);
	}

	/**
	 * Gets the rectangle float.
	 * 
	 * @return the rectangle float
	 */
	public Rectangle2D.Float getRectangleFloat()
	{
		Float[] rect = getFloatArray();

		if (rect.length != 4)
		{
			throw new UnknownVariableValueFormException();
		}

		return new Rectangle2D.Float(rect[0], rect[1], rect[2], rect[3]);
	}

	/**
	 * Gets the string.
	 * 
	 * @return the string
	 */
	public String getString()
	{
		return m_value;
	}

	/**
	 * Gets the int.
	 * 
	 * @return the int
	 */
	public int getInt()
	{
		return Integer.parseInt(getValueNoWhitespace());
	}

	/**
	 * Gets the float.
	 * 
	 * @return the float
	 */
	public float getFloat()
	{
		return Float.parseFloat(getValueNoWhitespace());
	}

	/**
	 * Gets the boolean.
	 * 
	 * @return the boolean
	 */
	public boolean getBoolean()
	{
		return Boolean.parseBoolean(getValueNoWhitespace());
	}

	/**
	 * Gets the int array.
	 * 
	 * @return the int array
	 */
	public Integer[] getIntArray()
	{
		ArrayList<Integer> buffer = new ArrayList<Integer>();

		String[] integers = getValueNoWhitespace().split(Pattern.quote(LIST_SPLIT));

		for (int i = 0; i < integers.length; i++)
		{
			if (!integers[i].isEmpty())
			{
				buffer.add(Integer.parseInt(integers[i]));
			}
		}

		return buffer.toArray(new Integer[buffer.size()]);
	}

	/**
	 * Gets the float array.
	 * 
	 * @return the float array
	 */
	public Float[] getFloatArray()
	{
		ArrayList<Float> buffer = new ArrayList<Float>();

		String[] floats = getValueNoWhitespace().replace("\r", "").split(Pattern.quote(LIST_SPLIT));

		for (int i = 0; i < floats.length; i++)
		{
			if (!floats[i].isEmpty())
			{
				buffer.add(Float.parseFloat(floats[i]));
			}
		}

		return buffer.toArray(new Float[buffer.size()]);
	}

	/**
	 * Gets the object name.
	 * 
	 * @return the object name
	 */
	public String getObjectName()
	{
		Matcher match = REGEX_OBJECTARGS.matcher(m_value);

		if (!match.find())
		{
			throw new UnknownVariableValueFormException();
		}

		return match.group("class");
	}

	/**
	 * Gets the object arguments.
	 * 
	 * @return the object arguments
	 */
	public VariableValue[] getObjectArguments()
	{
		ArrayList<VariableValue> arguments = new ArrayList<VariableValue>();

		Matcher match = REGEX_OBJECTARGS.matcher(m_value);

		if (!match.find())
		{
			throw new UnknownVariableValueFormException();
		}

		match = REGEX_ARGUMENT.matcher(match.group("arguments"));

		while (match.find())
		{
			arguments.add(new VariableValue(match.group("argument")));
		}

		return arguments.toArray(new VariableValue[arguments.size()]);
	}
}
