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

import jeva.communication.SharedEntity.SharedField;

public class PolicyViolationException extends RuntimeException
{
	
	private static final long serialVersionUID = 1L;

	public PolicyViolationException(SharedField<?> sharedField)
	{
		super("Network Policy Violation on field: " + sharedField.getId() + " policy is:" + sharedField.getPolicy().toString());
	}

	public PolicyViolationException(SharedEntity networkEntity, SharePolicy policy)
	{
		super("Network Policy Violation instantiating class: " + networkEntity.getClass().getCanonicalName() + " policy is:" + policy.toString());
	}

	public PolicyViolationException(String className, SharePolicy policy)
	{
		super("Network Policy Violation instantiating class: " + className + " policy is:" + policy.toString());
	}
}
