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
