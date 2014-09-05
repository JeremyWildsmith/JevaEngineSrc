package io.github.jevaengine.script;

import io.github.jevaengine.util.StaticSet;

import javax.inject.Inject;


public class ScriptEvent
{
	//Firing a handler can result in the registration/addition/removal of another handler
	//Thus we must use a staticset.
	private StaticSet<IFunction> m_listeners = new StaticSet<>();
	
	private IFunctionFactory m_functionFactory;
	
	@Inject
	public ScriptEvent(IFunctionFactory functionFactory)
	{
		m_functionFactory = functionFactory;
	}
	
	public void add(Object function) throws UnrecognizedFunctionException
	{
		m_listeners.add(m_functionFactory.wrap(function));
	}
	
	public void remove(Object function) throws UnrecognizedFunctionException
	{
		m_listeners.remove(m_functionFactory.wrap(function));
	}
	
	@ScriptHiddenMember
	public void fire(final Object ... arguments) throws ScriptExecuteException
	{
		for(final IFunction f : m_listeners)
			f.call(arguments);
	}
}
