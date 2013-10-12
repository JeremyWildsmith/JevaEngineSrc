package jeva;

/**
 * An exception thrown when the Core has failed to be initialized properly.
 */
public class CoreInitializationException extends RuntimeException
{
	/** Serial Version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new core initialization exception.
	 * 
	 * @param reason
	 *            A message describing the cause for the exception being thrown.
	 */
	public CoreInitializationException(String reason)
	{
		super(reason);
	}
}
