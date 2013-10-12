/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.config;

/**
 * The Class UnknownVariableException.
 * 
 * @author Scott
 */
public class UnknownVariableException extends RuntimeException
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new unknown variable exception.
	 * 
	 * @param variableName
	 *            the variable name
	 */
	public UnknownVariableException(String variableName)
	{
		super("Unknown Variable: " + variableName);
	}
}
