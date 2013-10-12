package jeva.communication;

public class SnapshotSynchronizationException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SnapshotSynchronizationException(InvalidFieldIdException ex)
	{
		super("Illegal refereance to field id: " + ex.toString());
	}

	public SnapshotSynchronizationException(PolicyViolationException ex)
	{
		super("Policy violation in attempting to synchronize snapshot: " + ex.toString());
	}
}
