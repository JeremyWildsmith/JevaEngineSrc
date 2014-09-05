package io.github.jevaengine.script;

public class ScriptDelegate<T>
{
	private IFunctionFactory m_functionFactory;
	private Class<T> m_retrieveType;
	private IFunction m_handler;
	
	private boolean m_allowsNull = false;
	
	public ScriptDelegate(IFunctionFactory functionFactory, Class<T> retrieveType)
	{
		m_functionFactory = functionFactory;
		m_retrieveType = retrieveType;
	}
	
	public ScriptDelegate(IFunctionFactory functionFactory, Class<T> retrieveType, boolean allowsNull)
	{
		this(functionFactory, retrieveType);
		m_allowsNull = allowsNull;
	}
	
	public void assign(Object function) throws UnrecognizedFunctionException
	{
		m_handler = m_functionFactory.wrap(function);
	}
	
	public boolean hasHandler()
	{
		return m_handler != null;
	}
	
	@SuppressWarnings("unchecked")
	@ScriptHiddenMember
	public T fire(final Object ... arguments) throws ScriptExecuteException
	{
		if(m_handler == null)
			throw new NoHandlerExistsException();

		Object o = m_handler.call(arguments);
		
		if(o == null)
		{
			if(m_allowsNull)
				return null;
			else
				throw new HandlerRetrievedInvalidValueException();
		
		} else
		{
			if(!m_retrieveType.isAssignableFrom(o.getClass()))
				throw new HandlerRetrievedInvalidValueException();
			
			return (T)o;
		}
	}
	
	public static final class HandlerRetrievedInvalidValueException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		private HandlerRetrievedInvalidValueException() { }
	}
	
	public static final class NoHandlerExistsException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;
		
		private NoHandlerExistsException() { }
	}
}
