/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
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
