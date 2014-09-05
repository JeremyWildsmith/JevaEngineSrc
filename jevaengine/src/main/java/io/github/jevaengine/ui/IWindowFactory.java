package io.github.jevaengine.ui;

import io.github.jevaengine.AssetConstructionException;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultWindowFactory.class)
public interface IWindowFactory
{
	Window create(String name, WindowBehaviourInjector behaviourInject) throws WindowConstructionException;
	Window create(String name) throws WindowConstructionException;
	
	public static final class WindowConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public WindowConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
