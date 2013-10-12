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
