package io.github.jevaengine.world.scene.model;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.world.scene.model.sprite.DefaultSceneModelFactory;

import com.google.inject.ImplementedBy;


@ImplementedBy(DefaultSceneModelFactory.class)
public interface ISceneModelFactory
{
	ISceneModel create(String name) throws SceneModelConstructionException;
	
	public static final class SceneModelConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public SceneModelConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
