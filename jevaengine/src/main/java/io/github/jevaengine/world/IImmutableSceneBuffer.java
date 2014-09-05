package io.github.jevaengine.world;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;

public interface IImmutableSceneBuffer extends IRenderable
{
	Vector2F translateScreenToWorld(Vector3F screenLocation, float scale);
	Vector2D translateWorldToScreen(Vector3F location, float scale);
	Vector2D translateWorldToScreen(Vector3F location);

	@Nullable
	<T extends IEntity> T pick(Class<T> clazz, int x, int y, float scale);
}
