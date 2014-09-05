package io.github.jevaengine.world.steering;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.physics.IImmutablePhysicsBody;

public final class FleeBehavior implements ISteeringBehavior
{
	private final float m_influence;
	private final float m_reactionDistance;
	
	private final ISteeringSubject m_target;
	
	public FleeBehavior(float influence, float reactionDistance, ISteeringSubject target)
	{
		m_influence = influence;
		m_reactionDistance = reactionDistance;
		m_target = target;
	}
		
	@Override
	public Vector2F direct(IImmutablePhysicsBody subject, Vector2F currentDirection)
	{
		Vector2F deltaFromDestination = subject.getLocation().getXy().difference(m_target.getLocation());
		
		if(deltaFromDestination.getLength() <= m_reactionDistance)
			return currentDirection;
		
		return currentDirection.add(deltaFromDestination.normalize().multiply(m_influence));
	}
}
