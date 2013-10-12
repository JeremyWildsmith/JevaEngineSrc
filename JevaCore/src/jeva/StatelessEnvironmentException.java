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
