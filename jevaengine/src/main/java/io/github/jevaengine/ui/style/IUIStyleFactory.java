package io.github.jevaengine.ui.style;

import io.github.jevaengine.AssetConstructionException;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultUIStyleFactory.class)
public interface IUIStyleFactory
{
	IUIStyle create(String name) throws UIStyleConstructionException;
	
	public static final class UIStyleConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public UIStyleConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
