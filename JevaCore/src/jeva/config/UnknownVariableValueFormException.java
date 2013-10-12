/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.config;

/**
 * The Class UnknownVariableValueFormException.
 * 
 * @author Scott
 */
public class UnknownVariableValueFormException extends RuntimeException
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new unknown variable value form exception.
	 * 
	 * @param form
	 *            the form
	 */
	UnknownVariableValueFormException(String form)
	{
		super("Unknow variable form taken as: " + form);
	}

	/**
	 * Instantiates a new unknown variable value form exception.
	 */
	UnknownVariableValueFormException()
	{
		super("Unknow variable form taken");
	}
}
