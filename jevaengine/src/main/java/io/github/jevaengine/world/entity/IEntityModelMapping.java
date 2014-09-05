package io.github.jevaengine.world.entity;

import io.github.jevaengine.IDisposable;

public interface IEntityModelMapping extends IDisposable
{
	void update(int deltaTime);
}
