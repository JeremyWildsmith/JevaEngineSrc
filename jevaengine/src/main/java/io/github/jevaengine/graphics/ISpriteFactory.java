package io.github.jevaengine.graphics;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.util.ThreadSafe;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultSpriteFactory.class)
public interface ISpriteFactory
{
	@ThreadSafe
	Sprite create(String path) throws SpriteConstructionException;
	
	public static final class SpriteConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public SpriteConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
