package io.github.jevaengine;

import io.github.jevaengine.util.ThreadSafe;

import java.io.InputStream;

public interface IAssetStreamFactory
{
	@ThreadSafe
	InputStream create(String name) throws AssetStreamConstructionException;
	
	public static final class AssetStreamConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public AssetStreamConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}	
	}
}
