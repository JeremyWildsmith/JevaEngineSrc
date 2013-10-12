package jeva.communication;

public class UnboundCommunicatorException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnboundCommunicatorException()
	{
		super("Operation requires binding to remote communicator");
	}
}
