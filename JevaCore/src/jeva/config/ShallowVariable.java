/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.config;

import java.util.ArrayList;

/**
 * The Class ShallowVariable.
 */
public class ShallowVariable extends Variable
{
	/** The m_children. */
	private ArrayList<Variable> m_children = new ArrayList<Variable>();

	/** The m_value. */
	private VariableValue m_value;

	/**
	 * Instantiates a new shallow variable.
	 * 
	 * @param parent
	 *            the parent
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 */
	public ShallowVariable(Variable parent, String name, VariableValue value)
	{
		super(parent, name);

		m_value = value;
		m_children = new ArrayList<Variable>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#getValue()
	 */
	@Override
	public final VariableValue getValue()
	{
		return m_value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#setValue(jeva.config.VariableValue)
	 */
	@Override
	public final void setValue(VariableValue value)
	{
		m_value = value;
	}

	/**
	 * Adds the child.
	 * 
	 * @param child
	 *            the child
	 */
	protected final void addChild(Variable child)
	{
		m_children.add(child);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#getChildren()
	 */
	@Override
	protected final Variable[] getChildren()
	{
		return m_children.toArray(new Variable[m_children.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#setChild(java.lang.String,
	 * jeva.config.VariableValue)
	 */
	@Override
	protected final Variable setChild(String name, VariableValue value)
	{
		getChild(name).setValue(value);

		return getChild(name);
	}
}
