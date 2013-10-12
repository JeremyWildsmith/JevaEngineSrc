package jeva.communication;

public class InvalidFieldIdException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFieldIdException(Class<?> owningClass, int id)
	{
		super("Invalid reference to field in class " + owningClass.getName() + " with id " + id);
	}
}
