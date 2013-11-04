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
package io.github.jevaengine.config;

import java.util.ArrayList;

public class ShallowVariable extends Variable
{

	private ArrayList<Variable> m_children = new ArrayList<Variable>();

	private VariableValue m_value;

	public ShallowVariable(Variable parent, String name, VariableValue value)
	{
		super(parent, name);

		m_value = value;
		m_children = new ArrayList<Variable>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.config.Variable#getValue()
	 */
	@Override
	public final VariableValue getValue()
	{
		return m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.config.Variable#setValue(jeva.config.VariableValue)
	 */
	@Override
	public final void setValue(VariableValue value)
	{
		m_value = value;
	}

	protected final void addChild(Variable child)
	{
		m_children.add(child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.config.Variable#getChildren()
	 */
	@Override
	protected final Variable[] getChildren()
	{
		return m_children.toArray(new Variable[m_children.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.config.Variable#setChild(java.lang.String,
	 * jeva.config.VariableValue)
	 */
	@Override
	protected final Variable setChild(String name, VariableValue value)
	{
		getChild(name).setValue(value);

		return getChild(name);
	}
}
