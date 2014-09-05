package io.github.jevaengine.world.steering;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.physics.IImmutablePhysicsBody;

public final class SeekBehavior implements ISteeringBehavior
{
	private final float ARRIVAL_TOLORANCE = 0.1F;
	private final float m_influence;
	private final ISteeringSubject m_target;
	
	public SeekBehavior(float influence, ISteeringSubject target)
	{
		m_influence = influence;
		m_target = target;
	}
		
	@Override
	public Vector2F direct(IImmutablePhysicsBody subject, Vector2F currentDirection)
	{
		Vector2F deltaFromDestination = m_target.getLocation().difference(subject.getLocation().getXy());
		
		if(deltaFromDestination.getLength() < ARRIVAL_TOLORANCE)
			return currentDirection;
		
		return currentDirection.add(deltaFromDestination.normalize().multiply(m_influence));
	}	
}
