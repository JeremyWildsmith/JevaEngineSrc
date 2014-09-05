package io.github.jevaengine.world.scene.model;

import io.github.jevaengine.world.Direction;

public interface ISceneModel extends IImmutableSceneModel
{
	ISceneModelAnimation getAnimation(String name) throws NoSuchAnimationException;

	void setDirection(Direction dir);
	Direction getDirection();
}
