/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva;

/**
 * Exception thrown when an IO error occurs when attempting to access a resource
 * library.
 */
public class ResourceLibraryIOException extends RuntimeException
{
	/** Serial Version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new file system IO exception.
	 * 
	 * @param resourceName
	 *            The name of the resource that could not be accessed.
	 */
	public ResourceLibraryIOException(String resourceName)
	{
		super("Filesystem IO Exception: " + resourceName);
	}
}
