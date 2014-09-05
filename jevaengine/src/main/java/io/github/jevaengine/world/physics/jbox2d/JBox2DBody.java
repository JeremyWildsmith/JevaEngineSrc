package io.github.jevaengine.world.physics.jbox2d;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.IPhysicsBodyObserver;
import io.github.jevaengine.world.physics.RayCastResults;

import org.jbox2d.collision.AABB;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;

public final class JBox2DBody implements IPhysicsBody
{
	private JBox2DWorld m_world;
	private Body m_body;
	private Fixture m_fixture;
	
	private float m_depth = 0.0F;
	
	private IEntity m_owner;
	private Observers m_observers = new Observers();
	
	public JBox2DBody(JBox2DWorld world, Body body, Fixture fixture, @Nullable IEntity owner)
	{
		m_body = body;
		m_fixture = fixture;
		m_owner = owner;
		m_body.m_userData = this;
		m_world = world;
	}
	
	public JBox2DBody(JBox2DWorld world, Body body, Fixture fixture)
	{
		this(world, body, fixture, null);
	}
	
	void beginContact(JBox2DBody other)
	{
		m_observers.onBeginContact(other);
	}
	
	void endContact(JBox2DBody other)
	{
		m_observers.onEndContact(other);		
	}
	
	@Override
	public void destory()
	{
		m_observers.clear();
		m_body.getWorld().destroyBody(m_body);
		m_world = null;
	}

	@Override
	public JBox2DWorld getWorld()
	{
		return m_world;
	}
	
	@Override
	@Nullable
	public RayCastResults castRay(Vector3F direction, float maxCast)
	{
		if(direction.isZero() || m_world == null)
			return null;
		
		Vector3F startPoint = getLocation().add(direction.normalize().multiply(m_fixture.getShape().getRadius()));
		Vector3F endPoint = startPoint.add(direction.normalize().multiply(maxCast));
		
		return new JBox2DRaycaster().cast(m_world, startPoint.getXy(), endPoint.getXy(), m_fixture);
	}
	
	@Override
	public void addObserver(IPhysicsBodyObserver o)
	{
		m_observers.add(o);
	}

	@Override
	public void removeObserver(IPhysicsBodyObserver o)
	{
		m_observers.add(o);
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
		return m_body.getType() == BodyType.STATIC;
	}
	
	@Override
	public boolean isCollidable()
	{
		return !m_fixture.isSensor();
	}
	
	@Override
	public Rect3F getAABB()
	{
		Rect3F aabb = new Rect3F();
		AABB b2aabb = m_fixture.getAABB(0);

		aabb.x = b2aabb.lowerBound.x;
		aabb.y = b2aabb.lowerBound.y;
		aabb.width = b2aabb.upperBound.x - b2aabb.lowerBound.x;
		aabb.height = b2aabb.upperBound.y - b2aabb.lowerBound.y;
		
		return aabb;
	}
	
	@Override
	public float getMass()
	{
		return m_body.getMass();
	}
	
	@Override
	public float getFriction()
	{
		return m_fixture.getFriction();
	}
	
	@Override
	public Vector3F getLocation()
	{
		return new Vector3F(JBox2DUtil.wrap(m_body.getWorldCenter()), m_depth);
	}

	@Override
	public Direction getDirection()
	{
		return Direction.fromAngle(m_body.getAngle());
	}

	@Override
	public Vector3F getLinearVelocity()
	{
		return new Vector3F(JBox2DUtil.wrap(m_body.getLinearVelocity()), 0);
	}
	
	@Override
	public void setLinearVelocity(Vector3F velocity)
	{
		m_body.setLinearVelocity(JBox2DUtil.unwrap(velocity.getXy()));
	}

	@Override
	public float getAngularVelocity()
	{
		return m_body.getAngularVelocity();
	}

	@Override
	public void setDirection(Direction direction)
	{
		m_body.setTransform(m_body.getWorldCenter(), direction.getAngle());	
		m_observers.directionSet();
	}

	@Override
	public void setLocation(Vector3F location)
	{
		m_body.setTransform(JBox2DUtil.unwrap(location.getXy()), m_body.getAngle());
		m_depth = location.z;
		m_observers.locationSet();
	}

	@Override
	public void applyLinearImpulse(Vector3F impulse)
	{
		m_body.applyLinearImpulse(JBox2DUtil.unwrap(impulse.getXy()), m_body.getWorldCenter());
	}
	
	@Override
	public void applyAngularImpulse(float impulse)
	{
		m_body.applyAngularImpulse(impulse);
	}
	
	@Override
	public void applyForceToCenter(Vector3F force)
	{
		m_body.applyForceToCenter(JBox2DUtil.unwrap(force.getXy()));
	}
	
	@Override
	public void applyTorque(float torque)
	{
		m_body.applyTorque(torque);
	}
	
	private static final class Observers extends StaticSet<IPhysicsBodyObserver>
	{
		public void locationSet()
		{
			for(IPhysicsBodyObserver o : this)
				o.locationSet();
		}
		
		public void directionSet()
		{
			for(IPhysicsBodyObserver o : this)
				o.directionSet();
		}
		
		public void onBeginContact(IPhysicsBody other)
		{
			for(IPhysicsBodyObserver o : this)
			{
				o.onBeginContact(other);
			}
		}
		
		public void onEndContact(IPhysicsBody other)
		{
			for(IPhysicsBodyObserver o : this)
			{
				o.onEndContact(other);
			}
		}
	}
}
