/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine;

import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public abstract class ResourceLibrary
{
	private ArrayList<EntityRegistration<? extends Entity>> m_registeredEntities = new ArrayList<EntityRegistration<? extends Entity>>();
	
	public abstract IVariable openConfiguration(String path);
	public abstract Script openScript(String path, Object context);
	
	public abstract InputStream openAsset(String path);

	protected final <T extends Entity> void registerEntity(String name, Class<T> clazz, IEntityFactory<T> factory)
	{
		EntityRegistration<?> e = find(name);
		
		if(e == null)
			m_registeredEntities.add(new EntityRegistration<T>(name, clazz, factory));
		else
			e.addFactory(factory);
	}

	@Nullable
	private EntityRegistration<? extends Entity> find(Class<? extends Entity> clazz)
	{
		for(EntityRegistration<? extends Entity> e : m_registeredEntities)
			if(e.getEntityClass().equals(clazz))
				return e;
		
		return null;
	}
	
	@Nullable
	private EntityRegistration<? extends Entity> find(String name)
	{
		for(EntityRegistration<? extends Entity> e : m_registeredEntities)
			if(e.getEntityClassName().equals(name))
				return e;
		
		return null;
	}
	
	public final Class<? extends Entity> lookupEntity(String className)
	{
		EntityRegistration<? extends Entity> e = find(className);
		
		if(e == null)
			throw new NoSuchElementException();
		
		return e.getEntityClass();
	}
	
	public final <T extends Entity> String lookupEntity(Class<T> entityClass)
	{
		EntityRegistration<? extends Entity> e = find(entityClass);
		
		if(e == null)
			throw new NoSuchElementException();
		
		return e.getEntityClassName();
	}
	
	public final <T extends Entity> T createEntity(Class<T> entityClass, @Nullable String instanceName, String config)
	{
		return (T)find(entityClass).createEntity(entityClass, instanceName, config);
	}
	
	public interface IEntityFactory<T>
	{
		T create(@Nullable IParentEntityFactory<T> lastFactory, @Nullable String instanceName, @Nullable String config);
	}
	
	public interface IParentEntityFactory<T>
	{
		T create(@Nullable String instanceName, @Nullable String config);
	}
	
	private static class EntityRegistration<T extends Entity>
	{
		private String m_name;
		private Class<T> m_class;
		private ArrayList<IEntityFactory<? extends Entity>> m_factories = new ArrayList<IEntityFactory<? extends Entity>>();
		
		public EntityRegistration(String name, Class<T> clazz, IEntityFactory<T> factory)
		{
			m_name = name;
			m_class = clazz;
			m_factories.add(factory);
		}
		
		public String getEntityClassName()
		{
			return m_name;
		}
		
		public Class<T> getEntityClass()
		{
			return m_class;
		}
		
		public <X extends Entity> void addFactory(IEntityFactory<X> factory)
		{
			m_factories.add(0, factory);
		}
		
		@SuppressWarnings("unchecked")
		public <X extends Entity> X createEntity(final int current, final Class<X> entityClass, final @Nullable String instanceName, final String config)
		{
			IEntityFactory<X> currentFactory = (IEntityFactory<X>)m_factories.get(current);
			
			if(current >= m_factories.size())
				return currentFactory.create(null, instanceName, config);
			else
				return currentFactory.create(new IParentEntityFactory<X>()
				{
					@Override
					public X create(String instanceName, String config) {
						return createEntity(current + 1, entityClass, instanceName, config);
					}
					
				}, instanceName, config);
		}
		
		public <X extends Entity> X createEntity(Class<X> entityClass, @Nullable String instanceName, String config)
		{
			return createEntity(0, entityClass, instanceName, config);
		}
		
		@Override
		public int hashCode()
		{
			return 0;
		}
	}
}
