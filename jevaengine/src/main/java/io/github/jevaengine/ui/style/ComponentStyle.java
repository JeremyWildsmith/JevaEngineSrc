package io.github.jevaengine.ui.style;

import java.util.NoSuchElementException;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.ui.ComponentState;

public final class ComponentStyle implements IDisposable
{
	private final ComponentStateStyle m_defaultStateStyle;
	private final ComponentStateStyle m_enterStateStyle;
	private final ComponentStateStyle m_activatedStateStyle;
	
	public ComponentStyle(ComponentStateStyle defaultStateStyle, ComponentStateStyle enterStateStyle, ComponentStateStyle activatedStateStyle)
	{
		m_defaultStateStyle = defaultStateStyle;
		m_enterStateStyle = enterStateStyle;
		m_activatedStateStyle = activatedStateStyle;
	}
	
	@Override
	public void dispose()
	{
		m_defaultStateStyle.dispose();
		m_enterStateStyle.dispose();
		m_activatedStateStyle.dispose();
	}
	
	public ComponentStateStyle getStateStyle(ComponentState state)
	{
		switch(state)
		{
		case Activated:
			return m_activatedStateStyle;
		case Default:
			return m_defaultStateStyle;
		case Enter:
			return m_enterStateStyle;
		default:
			throw new NoSuchElementException();
		}
	}
}