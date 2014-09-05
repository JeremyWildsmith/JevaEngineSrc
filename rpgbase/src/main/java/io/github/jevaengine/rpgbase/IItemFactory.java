package io.github.jevaengine.rpgbase;

import io.github.jevaengine.AssetConstructionException;

public interface IItemFactory
{
	Item create(ItemIdentifier identifier) throws ItemContructionException;
	
	public static final class ItemContructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public ItemContructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
		
	}
}
