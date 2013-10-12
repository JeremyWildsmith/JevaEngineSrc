/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.game;

/**
 * The Class ResourceLoadingException.
 * 
 * @author Scott
 */
public class ResourceLoadingException extends RuntimeException
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new resource loading exception.
	 * 
	 * @param resource
	 *            the resource
	 */
	public ResourceLoadingException(String resource)
	{
		super("Problems loading resource: " + resource);
	}
}
