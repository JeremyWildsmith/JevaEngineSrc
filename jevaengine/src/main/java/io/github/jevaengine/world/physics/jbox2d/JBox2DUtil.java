package io.github.jevaengine.world.physics.jbox2d;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.physics.PhysicsBodyDescription.PhysicsBodyType;

import java.util.NoSuchElementException;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

final class JBox2DUtil
{
	public static Vector2F wrap(Vec2 vec)
	{
		return new Vector2F(vec.x, vec.y);
	}
	
	public static Vec2 unwrap(Vector2F vec)
	{
		return new Vec2(vec.x, vec.y);
	}
	
	public static BodyType unwrap(PhysicsBodyType bodyType)
	{
		switch(bodyType)
		{
		case Dynamic:
			return BodyType.DYNAMIC;
		case Kinematic:
			return BodyType.KINEMATIC;
		case Static:
			return BodyType.STATIC;
		default:
			throw new NoSuchElementException();
		}
	}
}
