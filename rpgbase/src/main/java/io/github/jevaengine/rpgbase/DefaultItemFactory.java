package io.github.jevaengine.rpgbase;

import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.graphics.ISpriteFactory;
import io.github.jevaengine.graphics.ISpriteFactory.SpriteConstructionException;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.util.Nullable;

public final class DefaultItemFactory implements IItemFactory
{
	private final IConfigurationFactory m_configurationFactory;
	private final ISpriteFactory m_spriteFactory;
	private final IScriptFactory m_scriptFactory;
	private final IAudioClipFactory m_audioClipFactory;
	
	public DefaultItemFactory(IConfigurationFactory configurationFactory, ISpriteFactory spriteFactory, IScriptFactory scriptFactory, IAudioClipFactory audioClipFactory)
	{
		m_configurationFactory = configurationFactory;
		m_spriteFactory = spriteFactory;
		m_scriptFactory = scriptFactory;
		m_audioClipFactory = audioClipFactory;
	}
	
	@Override
	public Item create(ItemIdentifier identifier) throws ItemContructionException
	{
		try
		{
			ItemDeclaration itemDecl = m_configurationFactory.create(identifier.toString()).getValue(ItemDeclaration.class);
	
			Sprite graphic = m_spriteFactory.create(itemDecl.sprite);
			
			return new Item(m_scriptFactory, m_audioClipFactory, itemDecl.script, identifier, itemDecl.name, graphic, itemDecl.type, itemDecl.description);
		} catch (ValueSerializationException | ConfigurationConstructionException | SpriteConstructionException e)
		{
			throw new ItemContructionException(identifier.toString(), e);
		}
	}

	public static class ItemDeclaration implements ISerializable
	{
		public String name;
		public ItemType type;
		public String sprite;
		
		public String description;
		
		@Nullable
		public String script;

		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("name").setValue(name);
			target.addChild("type").setValue(type.ordinal());
			target.addChild("sprite").setValue(sprite);
			
			if(description != null && description.length() > 0)
				target.addChild("description").setValue(description);
			
			if(script != null && script.length() > 0)
				target.addChild("script").setValue(script);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				name = source.getChild("name").getValue(String.class);
				type = ItemType.values()[source.getChild("type").getValue(Integer.class)];
				sprite = source.getChild("sprite").getValue(String.class);
				
				if(source.childExists("description"))
					description = source.getChild("description").getValue(String.class);
				
				if(source.childExists("script"))
					script = source.getChild("script").getValue(String.class);
			} catch (NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}
	}
}
