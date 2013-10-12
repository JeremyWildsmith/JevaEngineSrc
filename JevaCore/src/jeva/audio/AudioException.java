package jeva.audio;

/**
 * Thrown an error occurs in the Audio subsystem.
 */
public class AudioException extends RuntimeException
{

	/** Serial Version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new audio exception.
	 * 
	 * @param reason
	 *            Reason\cause of the exception.
	 */
	public AudioException(String reason)
	{
		super(reason);
	}
}
