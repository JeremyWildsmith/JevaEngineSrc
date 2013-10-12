package jeva;

import javax.script.ScriptException;

/**
 * Exception thrown when an error occurs executing an engine script.
 */
public class CoreScriptException extends RuntimeException
{
	/** Serial Version UID. */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiates a new core script exception.
	 * 
	 * @param reason
	 *            The message describing the cause for the exception.
	 */
	public CoreScriptException(String reason)
	{
		super(reason);
	}

	/**
	 * Instantiates a new core script exception which generates an reason
	 * message for the specified ScriptException.
	 * 
	 * @param e
	 *            Exception to be wrapped.
	 */
	public CoreScriptException(ScriptException e)
	{
		super(String.format("%d: %s", e.getLineNumber(), e.getLocalizedMessage()));
	}
}
