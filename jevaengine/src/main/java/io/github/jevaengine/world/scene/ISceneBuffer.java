package io.github.jevaengine.world.scene;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.IImmutableSceneBuffer;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;

public interface ISceneBuffer extends IImmutableSceneBuffer
{		
	void addModel(IImmutableSceneModel model, @Nullable IEntity dispatcher, Vector3F location);
	void addModel(IImmutableSceneModel model, Vector3F location);
	void reset();
	
	void translate(Vector2D translation);
}
