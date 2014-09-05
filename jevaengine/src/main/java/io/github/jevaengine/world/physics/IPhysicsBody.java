package io.github.jevaengine.world.physics;

import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.world.Direction;


public interface IPhysicsBody extends IImmutablePhysicsBody
{
	void setLocation(Vector3F location);
	void setDirection(Direction direction);
	void applyLinearImpulse(Vector3F impulse);
	void applyAngularImpulse(float impulse);
	void applyForceToCenter(Vector3F force);
	void applyTorque(float torque);
	
	void setLinearVelocity(Vector3F velocity);
	
	void destory();
}
