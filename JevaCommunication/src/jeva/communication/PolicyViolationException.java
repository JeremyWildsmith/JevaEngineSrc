package jeva.communication;

import jeva.communication.SharedEntity.SharedField;

public class PolicyViolationException extends RuntimeException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PolicyViolationException(SharedField<?> sharedField)
	{
		super("Network Policy Violation on field: " + sharedField.getId() + " policy is:" + sharedField.getPolicy().toString());
	}

	public PolicyViolationException(SharedEntity networkEntity, SharePolicy policy)
	{
		super("Network Policy Violation instantiating class: " + networkEntity.getClass().getCanonicalName() + " policy is:" + policy.toString());
	}

	public PolicyViolationException(String blassName, SharePolicy policy)
	{
		super("Network Policy Violation instantiating class: " + blassName + " policy is:" + policy.toString());
	}
}
