package jeva.communication;

public class CommunicatorAlreadyBoundException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CommunicatorAlreadyBoundException()
	{
		super("Cannot bind a communicator when it has already been bound.");
	}
}
