package io.github.jevaengine.config;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.config.json.CachedJsonConfigurationFactory;
import io.github.jevaengine.util.ThreadSafe;

import com.google.inject.ImplementedBy;

@ImplementedBy(CachedJsonConfigurationFactory.class)
public interface IConfigurationFactory
{
	@ThreadSafe
	IVariable createMutable(String name) throws ConfigurationConstructionException;

	@ThreadSafe
	IImmutableVariable create(String name) throws ConfigurationConstructionException;
	
	public static final class ConfigurationConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public ConfigurationConstructionException(String assetName,
				Exception cause) {
			super(assetName, cause);
		}
	}
}
