package io.github.jevaengine.world.physics;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.entity.IEntity;


public interface IImmutablePhysicsBody
{
	IImmutablePhysicsWorld getWorld();
	
	boolean hasOwner();
	IEntity getOwner();
	
	boolean isStatic();
	boolean isCollidable();
	
	Rect3F getAABB();
	float getMass();
	Vector3F getLocation();
	Direction getDirection();
	Vector3F getLinearVelocity();	
	float getAngularVelocity();
	
	float getFriction();
	
	RayCastResults castRay(Vector3F direction, float maxCast);
	
	void addObserver(IPhysicsBodyObserver o);
	void removeObserver(IPhysicsBodyObserver o);
}
