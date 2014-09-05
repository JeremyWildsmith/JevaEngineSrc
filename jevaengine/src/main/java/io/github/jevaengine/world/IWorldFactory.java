package io.github.jevaengine.world;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.IInitializationProgressMonitor;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultWorldFactory.class)
public interface IWorldFactory
{
	World create(String name, float tileWidthMeters, float tileHeightMeters, IInitializationProgressMonitor progressMonitor) throws WorldConstructionException;

	public static final class WorldConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public WorldConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
