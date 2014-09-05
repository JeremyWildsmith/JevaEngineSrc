package io.github.jevaengine.world.steering;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.world.physics.IImmutablePhysicsBody;
import io.github.jevaengine.world.physics.RayCastResults;

import java.util.Random;

public final class AvoidanceBehavior implements ISteeringBehavior
{	
	private final float m_reactionDistance;
	private final boolean m_steerBias = new Random().nextBoolean();
	
	public AvoidanceBehavior(float reactionDistance)
	{
		m_reactionDistance = reactionDistance;
	}

	@Override
	public Vector2F direct(IImmutablePhysicsBody subject, Vector2F currentDirection)
	{
		if(currentDirection.isZero())
			return currentDirection;

		Vector2F travelDirection = currentDirection.normalize();

		RayCastResults resultsStraight = subject.castRay(new Vector3F(travelDirection, 0), m_reactionDistance);
		
		if(resultsStraight == null)
			return currentDirection;
		
		Vector2F avoidDirection = resultsStraight.getNormal().getXy().add(travelDirection);
		
		if(avoidDirection.isZero())
			avoidDirection = travelDirection.rotate(m_steerBias ? -1 : 1 * (float)Math.PI / 2);
		
		Vector2F steerDirection = avoidDirection.normalize();
		
		return steerDirection;
	}
}
