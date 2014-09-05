package io.github.jevaengine.ui;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.util.Nullable;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultControlFactory.class)
public interface IControlFactory
{
	@Nullable
	Class<? extends Control> lookup(String className);
	
	@Nullable
	<T extends Control> String lookup(Class<T> controlClass);
	
	<T extends Control> T create(Class<T> controlClass, @Nullable String instanceName, IImmutableVariable config) throws ControlConstructionException;
	Control create(String controlName, @Nullable String instanceName, IImmutableVariable config) throws ControlConstructionException;

	public static final class ControlConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public ControlConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}

}
