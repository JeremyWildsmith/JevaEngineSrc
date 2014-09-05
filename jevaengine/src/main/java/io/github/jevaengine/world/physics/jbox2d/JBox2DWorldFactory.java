package io.github.jevaengine.world.physics.jbox2d;

import io.github.jevaengine.world.physics.IPhysicsWorld;
import io.github.jevaengine.world.physics.IPhysicsWorldFactory;

public final class JBox2DWorldFactory implements IPhysicsWorldFactory
{
	@Override
	public IPhysicsWorld create(float maxSurfaceFrictionForceNewtonMeters, int worldWidthTiles, int worldHeightTiles)
	{
		return new JBox2DWorld(maxSurfaceFrictionForceNewtonMeters, worldWidthTiles, worldHeightTiles);
	}

}
