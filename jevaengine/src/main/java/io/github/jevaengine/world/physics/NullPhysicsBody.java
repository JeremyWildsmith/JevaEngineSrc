package io.github.jevaengine.world.physics;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.entity.DefaultEntity;

public final class NullPhysicsBody implements IPhysicsBody
{

	@Override
	public boolean hasOwner()
	{
		return false;
	}

	@Override
	public DefaultEntity getOwner()
	{
		return null;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public float getMass()
	{
		return 1;
	}
	
	@Override
	public float getFriction()
	{
		return 0;
	}

	@Override
	public Vector3F getLocation()
	{
		return new Vector3F();
	}

	@Override
	public Direction getDirection()
	{
		return Direction.Zero;
	}

	@Override
	public Vector3F getLinearVelocity()
	{
		return new Vector3F();
	}

	@Override
	public float getAngularVelocity()
	{
		return 0;
	}

	@Override
	@Nullable
	public RayCastResults castRay(Vector3F direction, float maxCast)
	{
		return null;
	}
	
	@Override
	public void addObserver(IPhysicsBodyObserver o) { }

	@Override
	public void removeObserver(IPhysicsBodyObserver o) { }

	@Override
	public void setLocation(Vector3F location) { }

	@Override
	public void setDirection(Direction direction) { }

	@Override
	public void applyLinearImpulse(Vector3F impulse) { }

	@Override
	public void applyAngularImpulse(float impulse) { }

	@Override
	public void applyForceToCenter(Vector3F force) { }

	@Override
	public void applyTorque(float torque) { }

	@Override
	public void setLinearVelocity(Vector3F velocity) { }

	@Override
	public void destory() { }

	@Override
	public IImmutablePhysicsWorld getWorld()
	{
		return new NullPhysicsWorld();
	}

	@Override
	public boolean isCollidable()
	{
		return false;
	}
	
	@Override
	public Rect3F getAABB()
	{
		return new Rect3F();
	}
}
