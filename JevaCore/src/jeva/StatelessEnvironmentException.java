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
package jeva;

/**
 * Thrown if a state is attempting to be saved or read to or from when the
 * environment which the core is running in is stateless.
 */
public class StatelessEnvironmentException extends Exception
{

	/** Serial Version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new stateless environment exception.
	 */
	public StatelessEnvironmentException()
	{
		super("Environment is statless");
	}
}
