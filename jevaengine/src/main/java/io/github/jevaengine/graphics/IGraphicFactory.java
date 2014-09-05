package io.github.jevaengine.graphics;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.util.ThreadSafe;

import com.google.inject.ImplementedBy;

@ImplementedBy(CachedBufferedGraphicFactory.class)
public interface IGraphicFactory
{
	@ThreadSafe
	IGraphic create(int width, int height);

	@ThreadSafe
	IImmutableGraphic create(String name) throws GraphicConstructionException;
	
	public static final class GraphicConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public GraphicConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
