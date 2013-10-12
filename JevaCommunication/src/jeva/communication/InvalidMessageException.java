package jeva.communication;

public class InvalidMessageException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Communicator m_sender;
	private Object m_message;

	public InvalidMessageException(Communicator sender, Object message, String reason)
	{
		super(reason);
		m_sender = sender;
		m_message = message;
	}

	public Object getSenderMessage()
	{
		return m_message;
	}

	public Communicator getSender()
	{
		return m_sender;
	}

}
