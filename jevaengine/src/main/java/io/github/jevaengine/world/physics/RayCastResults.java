package io.github.jevaengine.world.physics;

import io.github.jevaengine.math.Vector3F;

public class RayCastResults
{
	private final Vector3F m_normal;
	private final float m_distance;
	
	public RayCastResults(Vector3F normal, float distance)
	{
		m_normal = new Vector3F(normal);
		m_distance = distance;
	}
	
	public Vector3F getNormal()
	{
		return new Vector3F(m_normal);
	}
	
	public float getDistance()
	{
		return m_distance;
	}
}
