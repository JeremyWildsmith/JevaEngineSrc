package io.github.jevaengine.ui;

import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.util.StaticSet;

import java.awt.Graphics2D;

public class Timer extends Control
{
	private static final String COMPONENT_NAME = "timer";
	
	private final Observers m_observers = new Observers();
	
	public Timer(String instanceName)
	{
		super(COMPONENT_NAME, instanceName);
	}
	
	public Timer()
	{
		this(null);
	}
	
	public void addObserver(ITimerObserver o)
	{
		m_observers.add(o);
	}
	
	public void removeObserver(ITimerObserver o)
	{
		m_observers.remove(o);
	}
	
	@Override
	public boolean onMouseEvent(InputMouseEvent mouseEvent) { return false; }

	@Override
	public boolean onKeyEvent(InputKeyEvent keyEvent) { return false; }

	@Override
	public Rect2D getBounds()
	{
		return new Rect2D();
	}

	@Override
	public void update(int deltaTime)
	{
		m_observers.update(deltaTime);
	}

	@Override
	public void render(Graphics2D g, int x, int y, float scale) { }
	
	public interface ITimerObserver
	{
		void update(int deltaTime);
	}
	
	private static final class Observers extends StaticSet<ITimerObserver>
	{
		public void update(int deltaTime)
		{
			for(ITimerObserver o : this)
				o.update(deltaTime);
		}
	}
}
