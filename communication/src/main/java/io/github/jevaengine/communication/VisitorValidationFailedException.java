package io.github.jevaengine.communication;



public class VisitorValidationFailedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public VisitorValidationFailedException(String reason){
		super(reason);
	}
}
