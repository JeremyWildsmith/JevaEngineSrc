package io.github.jevaengine.world.physics;

public class NullPhysicsWorldFactory implements IPhysicsWorldFactory
{
	@Override
	public IPhysicsWorld create(float maxSurfaceFrictionForceNewtonMeters, int worldWidthTiles, int worldHeightTiles)
	{
		return new NullPhysicsWorld(maxSurfaceFrictionForceNewtonMeters);
	}

}
