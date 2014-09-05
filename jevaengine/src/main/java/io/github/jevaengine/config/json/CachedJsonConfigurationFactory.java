package io.github.jevaengine.config.json;

import io.github.jevaengine.IAssetStreamFactory;
import io.github.jevaengine.IAssetStreamFactory.AssetStreamConstructionException;
import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.ThreadSafe;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.inject.Inject;

public final class CachedJsonConfigurationFactory implements IConfigurationFactory
{
	private HashMap<String, SoftReference<IImmutableVariable>> m_varCache = new HashMap<String, SoftReference<IImmutableVariable>>();
	
	private final IAssetStreamFactory m_assetFactory;
	
	@Inject
	public CachedJsonConfigurationFactory(IAssetStreamFactory assetFactory)
	{
		m_assetFactory = assetFactory;
	}
	
	@ThreadSafe
	@Nullable
	private IImmutableVariable findCachedConfiguration(String formal)
	{
		synchronized(m_varCache)
		{
			Iterator<Map.Entry<String, SoftReference<IImmutableVariable>>> it = m_varCache.entrySet().iterator();
	
			while(it.hasNext())
			{
				Map.Entry<String, SoftReference<IImmutableVariable>> entry = it.next();
				
				if(entry.getValue().get() != null)
				{
					if(entry.getKey().equals(formal))
						return entry.getValue().get();
				}else
					it.remove();
			}
		
			return null;
		}
	}

	@Override
	@ThreadSafe
	public IImmutableVariable create(String name) throws ConfigurationConstructionException
	{
		/*
		synchronized(m_varCache)
		{
			String formal = name.trim().replace("\\", "/");
			
			IImmutableVariable cacheVar = findCachedConfiguration(formal);
			
			if(cacheVar != null)
				return cacheVar;
			else
			{
				IImmutableVariable loadVar = createMutable(formal);

				m_varCache.put(formal, new SoftReference<IImmutableVariable>(loadVar));
	
				return loadVar;
			}
		}*/
		
		return createMutable(name);
	}
	
	@Override
	@ThreadSafe
	public IVariable createMutable(String name) throws ConfigurationConstructionException
	{
		try {
			return JsonVariable.create(m_assetFactory.create(name));
		} catch (IOException | ValueSerializationException | AssetStreamConstructionException e) {
			throw new ConfigurationConstructionException(name, e);
		}
	}
}
