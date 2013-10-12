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
