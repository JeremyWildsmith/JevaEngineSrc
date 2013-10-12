package jeva.config;

/**
 * The Class VariableStoreSerializationException.
 */
public class VariableStoreSerializationException extends RuntimeException
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new variable store serialization exception.
	 * 
	 * @param reason
	 *            the reason
	 */
	public VariableStoreSerializationException(String reason)
	{
		super("Error serializing variable store: " + reason);
	}

}
