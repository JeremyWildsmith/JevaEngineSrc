package io.github.jevaengine.world.scene.model;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.world.Direction;

import java.util.ArrayList;
import java.util.List;

public final class NullSceneModel implements ISceneModel
{
	@Override
	public ISceneModel clone()
	{
		return new NullSceneModel();
	}
	
	@Override
	public List<ISceneModelComponent> getComponents()
	{
		return new ArrayList<>();
	}

	@Override
	public void addObserver(ISceneModelObserver o) { }

	@Override
	public void removeObserver(ISceneModelObserver o) { }
	
	@Override
	public ISceneModelAnimation getAnimation(String name) throws NoSuchAnimationException
	{
		throw new NoSuchAnimationException(name);
	}

	@Override
	public void update(int delta) { }

	@Override
	public void setDirection(Direction dir) { }

	@Override
	public Direction getDirection()
	{
		return Direction.Zero;
	}

	@Override
	public Rect3F getAABB()
	{
		return new Rect3F();
	}
}
