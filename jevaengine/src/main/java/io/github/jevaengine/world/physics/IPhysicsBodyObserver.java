package io.github.jevaengine.world.physics;


public interface IPhysicsBodyObserver
{
	void locationSet();
	void directionSet();
	
	void onBeginContact(IImmutablePhysicsBody other);
	void onEndContact(IImmutablePhysicsBody other);
}
