package io.github.jevaengine.world.steering;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.physics.IPhysicsBody;

import java.util.ArrayList;

public final class VelocityLimitSteeringDriver implements ISteeringDriver
{
	private final ArrayList<ISteeringBehavior> m_appliedBehaviors = new ArrayList<>();

	private final float m_maxSteerVelocity;
	
	@Nullable
	private IPhysicsBody m_target;
	
	public VelocityLimitSteeringDriver(float maxSteerVelocity)
	{
		m_maxSteerVelocity = maxSteerVelocity;
	}
	
	@Override
	public void add(ISteeringBehavior b)
	{
		m_appliedBehaviors.add(b);
	}
	
	@Override
	public void remove(ISteeringBehavior b)
	{
		m_appliedBehaviors.remove(b);
	}

	@Override
	public void clear()
	{
		m_appliedBehaviors.clear();
	}
	
	@Override
	public void attach(IPhysicsBody target)
	{
		m_target = target;
	}

	@Override
	public void dettach()
	{
		m_target = null;
	}
	
	@Override
	public boolean isDriving()
	{
		if(m_target == null)
			return false;
		
		return !getSteerVelocity().isZero();
	}

	private Vector2F getSteerVelocity()
	{	
		Vector2F steerDirection = new Vector2F();
	
		if(m_target == null)
			return steerDirection;
		
		for(ISteeringBehavior b : m_appliedBehaviors)
			steerDirection = b.direct(m_target, steerDirection);
		
		steerDirection = steerDirection.isZero() ? steerDirection : steerDirection.normalize();
		
		return steerDirection.multiply(m_maxSteerVelocity);
	}
	
	@Override
	public void update(int deltaTime)
	{	
		if(m_target == null)
			return;
		
		Vector2F steerVelocity = getSteerVelocity().difference(m_target.getLinearVelocity().getXy());
		
		if(steerVelocity.isZero())
			return;
	
		m_target.applyLinearImpulse(new Vector3F(steerVelocity.multiply(m_target.getMass())
				.add(steerVelocity.normalize().multiply(m_target.getWorld().getMaxFrictionForce())), 0).multiply(deltaTime / 1000.0F));
		
		m_target.setDirection(Direction.fromVector(steerVelocity));
	}
}
