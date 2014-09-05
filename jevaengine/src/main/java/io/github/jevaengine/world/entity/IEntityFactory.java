package io.github.jevaengine.world.entity;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.util.Nullable;

public interface IEntityFactory
{
	@Nullable
	Class<? extends IEntity> lookup(String className);
	
	@Nullable
	<T extends IEntity> String lookup(Class<T> entityClass);
	
	<T extends IEntity> T create(Class<T> entityClass, @Nullable String instanceName, IImmutableVariable config) throws EntityConstructionException;
	IEntity create(String entityName, @Nullable String instanceName, IImmutableVariable config) throws EntityConstructionException;

	<T extends IEntity> T create(Class<T> entityClass, @Nullable String instanceName, String config) throws EntityConstructionException;
	IEntity create(String entityName, @Nullable String instanceName, String config) throws EntityConstructionException;

	public static final class EntityConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public EntityConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
		
	}

}
