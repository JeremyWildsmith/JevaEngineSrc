package io.github.jevaengine.world.steering;

public final class VelocityLimitSteeringDriverFactory implements ISteeringDriverFactory
{
	private final float m_maxVelocity;
	
	public VelocityLimitSteeringDriverFactory(float maxVelocity)
	{
		m_maxVelocity = maxVelocity;
	}
	
	@Override
	public ISteeringDriver create()
	{
		return new VelocityLimitSteeringDriver(m_maxVelocity);
	}

}
