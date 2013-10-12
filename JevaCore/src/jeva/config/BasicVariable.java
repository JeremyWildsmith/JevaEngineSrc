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
package jeva.config;

/**
 * The Class BasicVariable.
 */
public class BasicVariable extends ShallowVariable
{

	/**
	 * Instantiates a new basic variable.
	 */
	public BasicVariable()
	{
		super(null, "", new VariableValue());
	}

	/**
	 * Instantiates a new basic variable.
	 * 
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public BasicVariable(String name, VariableValue value)
	{
		super(null, name, value);
	}

	/**
	 * Instantiates a new basic variable.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public BasicVariable(Variable parent, String name, VariableValue value)
	{
		super(parent, name, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#createChild(java.lang.String,
	 * jeva.config.VariableValue)
	 */
	@Override
	public final BasicVariable createChild(String name, VariableValue value)
	{
		BasicVariable var = new BasicVariable(this, name, value);

		super.addChild(var);

		return var;
	}
}
