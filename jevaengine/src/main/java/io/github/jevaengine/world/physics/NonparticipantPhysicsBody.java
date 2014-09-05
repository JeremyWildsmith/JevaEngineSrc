package io.github.jevaengine.world.physics;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.entity.IEntity;

public final class NonparticipantPhysicsBody implements IPhysicsBody
{
	private final IEntity m_owner;
	private Vector3F m_location = new Vector3F();
	private Direction m_direction = Direction.Zero;

	private Observers m_observers = new Observers();
	
	public NonparticipantPhysicsBody(IEntity owner)
	{
		m_owner = owner;
	}

	public NonparticipantPhysicsBody()
	{
		m_owner = null;
	}
	
	@Override
	public void destory()
	{
		m_observers.clear();
	}
	
	@Override
	public IImmutablePhysicsWorld getWorld()
	{
		return new NullPhysicsWorld();
	}

	@Override
	public boolean hasOwner()
	{
		return m_owner != null;
	}
	
	@Override
	public IEntity getOwner()
	{
		return m_owner;
	}

	@Override
	public boolean isStatic()
	{
		return true;
	}
	
	@Override
	public float getMass()
	{
		return 1.0F;
	}
	
	@Override
	public float getFriction()
	{
		return 0;
	}
	
	@Override
	public Vector3F getLocation()
	{
		return m_location;
	}

	@Override
	public Direction getDirection()
	{
		return m_direction;
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
	public void addObserver(IPhysicsBodyObserver o)
	{
		m_observers.add(o);
	}

	@Override
	public void removeObserver(IPhysicsBodyObserver o)
	{
		m_observers.remove(o);
	}

	@Override
	public void setLocation(Vector3F location)
	{
		m_location = new Vector3F(location);
		m_observers.locationSet();
	}

	@Override
	public void setDirection(Direction direction)
	{
		m_direction = direction;
		m_observers.directionSet();
	}
	
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
	
	private static final class Observers extends StaticSet<IPhysicsBodyObserver>
	{
		void locationSet()
		{
			for(IPhysicsBodyObserver o : this)
				o.locationSet();
		}
		
		void directionSet()
		{
			for(IPhysicsBodyObserver o : this)
				o.directionSet();
		}
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
