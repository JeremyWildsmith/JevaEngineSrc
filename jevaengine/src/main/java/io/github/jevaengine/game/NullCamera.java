package io.github.jevaengine.game;

import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.world.IImmutableSceneBuffer;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.scene.NullSceneBuffer;

public final class NullCamera implements ICamera
{

	@Override
	public IImmutableSceneBuffer getScene(Rect2D bounds, float scale)
	{
		return new NullSceneBuffer();
	}

	@Override
	public void dettach() { }

	@Override
	public void attach(World world) { }

	@Override
	public Vector3F getLookAt()
	{
		return new Vector3F();
	}
}
