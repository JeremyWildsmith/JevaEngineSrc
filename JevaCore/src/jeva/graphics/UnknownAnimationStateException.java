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
package jeva.graphics;

/**
 * The Class UnknownAnimationStateException.
 * 
 * @author Scott
 */
public class UnknownAnimationStateException extends RuntimeException
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new unknown animation state exception.
	 * 
	 * @param state
	 *            the state
	 */
	public UnknownAnimationStateException(AnimationState state)
	{
		super("Unknown ANimation State: " + state);
	}
}
