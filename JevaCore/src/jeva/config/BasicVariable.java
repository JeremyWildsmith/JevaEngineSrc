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
