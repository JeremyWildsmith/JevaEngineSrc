package io.github.jevaengine.world.physics.jbox2d;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.physics.RayCastResults;

import java.util.Arrays;
import java.util.HashSet;

import org.jbox2d.callbacks.RayCastCallback;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Fixture;

final class JBox2DRaycaster
{
	private HashSet<Fixture> m_ignoreSet = new HashSet<>();
	private Vector2F m_normal;
	private float m_distance;
	private boolean m_intersected = false;

	@Nullable
	public RayCastResults cast(JBox2DWorld world, Vector2F start, Vector2F end, Fixture ... ignoreSet)
	{
		m_ignoreSet.clear();
		m_ignoreSet.addAll(Arrays.asList(ignoreSet));
		m_normal = new Vector2F();
		m_distance = Float.MAX_VALUE;
		m_intersected = false;
		world.getWorld().raycast(new CallbackHandler(start), JBox2DUtil.unwrap(start), JBox2DUtil.unwrap(end));
		
		if(!m_intersected)
			return null;
		else
			return new RayCastResults(new Vector3F(m_normal, 0), m_distance);
	}
	
	private class CallbackHandler implements RayCastCallback
	{
		private final Vector2F m_source;
		
		public CallbackHandler(Vector2F source)
		{
			m_source = source;
		}
		
		@Override
		public float reportFixture(Fixture fixture, Vec2 point, Vec2 normal, float fraction)
		{
			float distance = m_source.difference(JBox2DUtil.wrap(point)).getLength();
			
			if(distance <= m_distance && !fixture.isSensor() && !m_ignoreSet.contains(fixture))
			{
				m_distance = distance;
				m_normal = JBox2DUtil.wrap(normal);
				m_intersected = true;
			}
			
			return 1.0F;
		}
		
	}
}
