package jeva;

/**
 * Thrown if a given resource path or name cannot be resolved to a resource in
 * the resource library.
 */
public class UnresolvedResourcePathException extends ResourceLibraryIOException
{

	/** Serial Version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new unresolved resource path exception.
	 * 
	 * @param file
	 *            Name of the file\resources which could not be resolved.
	 */
	public UnresolvedResourcePathException(String file)
	{
		super("Unable to resolve file path: " + file);
	}
}
