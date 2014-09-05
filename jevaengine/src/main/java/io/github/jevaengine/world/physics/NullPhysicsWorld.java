package io.github.jevaengine.world.physics;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.entity.IEntity;

public final class NullPhysicsWorld implements IPhysicsWorld
{
	private final float m_maxFrictionForce;
	
	public NullPhysicsWorld(float maxFrictionForce)
	{
		m_maxFrictionForce = maxFrictionForce;
	}

	public NullPhysicsWorld()
	{
		this(0);
	}
	
	@Override
	public void update(int deltaTime) { }

	@Override
	public void setGravity(Vector2F gravity) { }

	@Override
	public IPhysicsBody createBody(IEntity owner, PhysicsBodyDescription bodyDescription)
	{
		return new NonparticipantPhysicsBody(owner);
	}

	@Override
	public IPhysicsBody createBody(PhysicsBodyDescription bodyDescription)
	{
		return new NonparticipantPhysicsBody();
	}

	@Override
	public float getMaxFrictionForce()
	{
		return m_maxFrictionForce;
	}
}
