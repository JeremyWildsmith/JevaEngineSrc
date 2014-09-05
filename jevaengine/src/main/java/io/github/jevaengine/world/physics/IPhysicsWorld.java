package io.github.jevaengine.world.physics;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.entity.IEntity;

public interface IPhysicsWorld extends IImmutablePhysicsWorld
{
	void update(int deltaTime);
	
	void setGravity(Vector2F gravity);
	
	IPhysicsBody createBody(IEntity owner, PhysicsBodyDescription bodyDescription);
	IPhysicsBody createBody(PhysicsBodyDescription bodyDescription);
}
