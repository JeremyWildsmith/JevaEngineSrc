/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

/**
 * The Class EntityInstantiationException.
 * 
 * @author Scott
 */
public class EntityInstantiationException extends RuntimeException
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new entity instantiation exception.
	 * 
	 * @param reason
	 *            the reason
	 */
	public EntityInstantiationException(String reason)
	{
		super("Unable to instantiate entity because " + reason);
	}
}
