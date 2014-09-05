package io.github.jevaengine.script;

import java.util.ArrayList;
import java.util.List;

public final class ArrayScriptDelegate<T>
{
	private IFunctionFactory m_functionFactory;
	private Class<T> m_retrieveType;
	private IFunction m_handler;
	
	private boolean m_allowsNull = false;
	
	public ArrayScriptDelegate(IFunctionFactory functionFactory, Class<T> retrieveType)
	{
		m_functionFactory = functionFactory;
		m_retrieveType = retrieveType;
	}
	
	public ArrayScriptDelegate(IFunctionFactory functionFactory, Class<T> retrieveType, boolean allowsNull)
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
	public List<T> fire(final Object ... arguments) throws ScriptExecuteException
	{
		if(m_handler == null)
			throw new NoHandlerExistsException();

		Object o = m_handler.call(arguments);
		
		if(o == null && m_allowsNull)
			return new ArrayList<T>();
		else if(!(o instanceof IScriptArray))
			throw new HandlerRetrievedInvalidValueException();
			
		IScriptArray scriptArray = (IScriptArray)o;

		ArrayList<T> elements = new ArrayList<>();

		for (int i = 0; i < scriptArray.getLength(); i++)
		{
			Object element = scriptArray.getElement(i);

			if (!(m_retrieveType.isAssignableFrom(element.getClass())))
				throw new HandlerRetrievedInvalidValueException();
			
			elements.add((T)element);
		}
		
		return elements;
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
