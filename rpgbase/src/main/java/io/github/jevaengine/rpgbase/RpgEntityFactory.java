package io.github.jevaengine.rpgbase;

import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.rpgbase.character.DefaultCharacterModelMappingFactory;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.rpgbase.dialogue.IDialogueRouteFactory;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.IEntityFactory;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;

import javax.inject.Inject;

public class RpgEntityFactory implements IEntityFactory
{
	private final IScriptFactory m_scriptFactory;
	private final IAudioClipFactory m_audioClipFactory;
	private final ISceneModelFactory m_modelFactory;
	private final IConfigurationFactory m_configurationFactory;
	private final IDialogueRouteFactory m_dialogueRouteFactory;
	
	@Inject
	public RpgEntityFactory(IScriptFactory scriptFactory, IAudioClipFactory audioClipFactory, ISceneModelFactory sceneModelFactory, IConfigurationFactory configurationFactory,
			IDialogueRouteFactory dialogueRouteFactory)
	{
		m_scriptFactory = scriptFactory;
		m_audioClipFactory = audioClipFactory;
		m_modelFactory = sceneModelFactory;
		m_configurationFactory = configurationFactory;
		m_dialogueRouteFactory = dialogueRouteFactory;
	}
	
	@Override
	@Nullable
	public Class<? extends IEntity> lookup(String className)
	{
		if(className.equals("character"))
			return RpgCharacter.class;
		else if(className.equals("areaTrigger"))
			return AreaTrigger.class;
	
		return null;
	}

	@Override
	@Nullable
	public <T extends IEntity> String lookup(Class<T> entityClass)
	{
		if(entityClass.equals(RpgCharacter.class))
			return "character";
		else if(entityClass.equals(AreaTrigger.class))
			return "areaTrigger";

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IEntity> T create(Class<T> entityClass, String instanceName, IImmutableVariable config) throws EntityConstructionException
	{
		try
		{
			if(entityClass.equals(RpgCharacter.class))
				return (T)new RpgCharacter(m_scriptFactory, m_audioClipFactory, m_modelFactory, new DefaultCharacterModelMappingFactory(), m_dialogueRouteFactory, instanceName, config);
			else if(entityClass.equals(AreaTrigger.class))
				return (T)new AreaTrigger(m_scriptFactory, instanceName, config);
		} catch (ValueSerializationException e)
		{
			throw new EntityConstructionException(instanceName, e);
		}
		
		throw new EntityConstructionException(instanceName, new UnsupportedEntityException());
	}

	@Override
	public IEntity create(String entityName, String instanceName, IImmutableVariable config) throws EntityConstructionException
	{
		Class<? extends IEntity> entityClass = lookup(entityName);
		
		if(entityClass == null)
			throw new EntityConstructionException(instanceName, new UnsupportedEntityException());

		return create(entityClass, instanceName, config);
	}
	
	public final class UnsupportedEntityException extends Exception
	{
		private static final long serialVersionUID = 1L;
		
		private UnsupportedEntityException() { }
	}

	@Override
	public <T extends IEntity> T create(Class<T> entityClass, String instanceName, String config) throws EntityConstructionException
	{
		try {
			return create(entityClass, instanceName, m_configurationFactory.create(config));
		} catch (ConfigurationConstructionException e) {
			throw new EntityConstructionException(instanceName, e);
		}
	}

	@Override
	public IEntity create(String entityName, String instanceName, String config) throws EntityConstructionException
	{
		Class<? extends IEntity> entityClass = lookup(entityName);
		
		if(entityClass == null)
			throw new EntityConstructionException(instanceName, new UnsupportedEntityException());

		return create(entityClass, instanceName, config);
	}
}
