package io.github.jevaengine.world.physics;

import io.github.jevaengine.world.physics.jbox2d.JBox2DWorldFactory;

import com.google.inject.ImplementedBy;

@ImplementedBy(JBox2DWorldFactory.class)
public interface IPhysicsWorldFactory
{
	IPhysicsWorld create(float maxSurfaceFrictionForceNewtonMeters, int worldWidthTiles, int worldHeightTiles);
}
