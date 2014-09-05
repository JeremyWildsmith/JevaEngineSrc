package io.github.jevaengine.world.steering;

import io.github.jevaengine.world.physics.IPhysicsBody;

public interface ISteeringDriver
{
	void add(ISteeringBehavior b);
	void remove(ISteeringBehavior b);
	void clear();
	
	void attach(IPhysicsBody target);
	void dettach();
	
	void update(int deltaTime);
	
	boolean isDriving();
}
