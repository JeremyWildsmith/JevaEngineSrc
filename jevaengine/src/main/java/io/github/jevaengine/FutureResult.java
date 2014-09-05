package io.github.jevaengine;

public final class FutureResult<T, Y extends Exception>
{
	private Y m_lastException;
	private T m_result;

	public FutureResult(Y error)
	{
		m_lastException = error;
	}
	
	public FutureResult(T result)
	{
		m_result = result;
	}
	
	public T get() throws Y
	{
		if(m_lastException != null)
			throw m_lastException;
		else
			return m_result;
	}
}
