package io.github.jevaengine.world.steering;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.physics.IImmutablePhysicsBody;

public interface ISteeringBehavior
{
	Vector2F direct(IImmutablePhysicsBody subject, Vector2F currentDirection);
}
