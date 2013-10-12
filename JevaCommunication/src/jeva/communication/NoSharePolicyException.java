package jeva.communication;

public class NoSharePolicyException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoSharePolicyException()
	{
		super("Object does not have a share policy");
	}
}
