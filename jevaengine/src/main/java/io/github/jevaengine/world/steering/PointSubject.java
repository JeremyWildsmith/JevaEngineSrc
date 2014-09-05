package io.github.jevaengine.world.steering;

import io.github.jevaengine.math.Vector2F;

public class PointSubject implements ISteeringSubject
{
	private Vector2F m_location;
	
	public PointSubject(Vector2F location)
	{
		m_location = new Vector2F(location);
	}

	public void setLocation(Vector2F location)
	{
		m_location = new Vector2F(location);
	}
	
	@Override
	public Vector2F getLocation()
	{
		return new Vector2F(m_location);
	}
}
