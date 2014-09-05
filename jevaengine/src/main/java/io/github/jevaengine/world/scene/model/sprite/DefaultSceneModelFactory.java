package io.github.jevaengine.world.scene.model.sprite;

import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.graphics.ISpriteFactory;
import io.github.jevaengine.graphics.ISpriteFactory.SpriteConstructionException;
import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelAnimation.SceneModelAnimationState;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.NoSuchAnimationException;
import io.github.jevaengine.world.scene.model.ISceneModel;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;
import io.github.jevaengine.world.scene.model.sprite.DefaultSceneModel.DefaultSceneModelAnimation;
import io.github.jevaengine.world.scene.model.sprite.DefaultSceneModelFactory.DefaultSceneModelDeclaration.DefaultSceneModelAnimationDeclaration;
import io.github.jevaengine.world.scene.model.sprite.DefaultSceneModelFactory.DefaultSceneModelDeclaration.DefaultSceneModelAnimationDeclaration.DefaultSceneModelAnimationDirectionDeclaration;
import io.github.jevaengine.world.scene.model.sprite.DefaultSceneModelFactory.DefaultSceneModelDeclaration.DefaultSceneModelAnimationDeclaration.DefaultSceneModelAnimationDirectionDeclaration.DefaultSceneModelAnimationComponentDeclaration;

import javax.inject.Inject;

public final class DefaultSceneModelFactory implements ISceneModelFactory
{
	private final IConfigurationFactory m_configurationFactory;
	private final ISpriteFactory m_spriteFactory;
	
	@Inject
	public DefaultSceneModelFactory(IConfigurationFactory configurationFactory, ISpriteFactory spriteFactory)
	{
		m_configurationFactory = configurationFactory;
		m_spriteFactory = spriteFactory;
 	}
	
	@Override
	public ISceneModel create(String name) throws SceneModelConstructionException
	{
		try
		{
			DefaultSceneModelDeclaration modelDecl = m_configurationFactory.create(name).getValue(DefaultSceneModelDeclaration.class);
		
			DefaultSceneModel model = new DefaultSceneModel();
			
			for(DefaultSceneModelAnimationDeclaration a : modelDecl.animations)
			{
				DefaultSceneModelAnimation animation = model.new DefaultSceneModelAnimation();
				
				for(DefaultSceneModelAnimationDirectionDeclaration d : a.sets)
				{
					for(Direction direction : d.directions)
					{
						for(DefaultSceneModelAnimationComponentDeclaration c : d.components)
						{
							DefaultSceneModelComponent component = new DefaultSceneModelComponent(
												m_spriteFactory.create(c.sprite),
												c.animation,
												c.bounds
											);
							
							animation.addComponent(direction, component);
						}
					}
				}
				
				model.addAnimation(a.name, animation);
			}
			
			model.getAnimation(modelDecl.defaultAnimation).setState(SceneModelAnimationState.Play);
			
			return model;
		} catch (ConfigurationConstructionException | ValueSerializationException | 
				 SpriteConstructionException | NoSuchAnimationException e)
		{
			throw new SceneModelConstructionException(name, e);
		}
	}

	public static final class DefaultSceneModelDeclaration implements ISerializable
	{
		public DefaultSceneModelAnimationDeclaration[] animations;
		public String defaultAnimation;
		
		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("animations").setValue(animations);
			target.addChild("defaultAnimation").setValue(defaultAnimation);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try {
				animations = source.getChild("animations").getValues(DefaultSceneModelAnimationDeclaration[].class);
				defaultAnimation = source.getChild("defaultAnimation").getValue(String.class);
			} catch (NoSuchChildVariableException e) {
				throw new ValueSerializationException(e);
			}
		}
		
		public static final class DefaultSceneModelAnimationDeclaration implements ISerializable
		{
			public String name;
			public DefaultSceneModelAnimationDirectionDeclaration[] sets;
			
			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				target.addChild("name").setValue(name);
				target.addChild("sets").setValue(sets);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					name = source.getChild("name").getValue(String.class);
					sets = source.getChild("sets").getValues(DefaultSceneModelAnimationDirectionDeclaration[].class);
				} catch (NoSuchChildVariableException e) {
					throw new ValueSerializationException(e);
				}
			}
			
			public static final class DefaultSceneModelAnimationDirectionDeclaration implements ISerializable
			{
				public Direction[] directions;
				public DefaultSceneModelAnimationComponentDeclaration[] components;
				
				@Override
				public void serialize(IVariable target) throws ValueSerializationException
				{
					Integer dirBuffer[] = new Integer[directions.length];
					
					for(int i = 0; i < directions.length; i++)
						dirBuffer[i] = directions[i].ordinal();
					
					target.addChild("directions").setValue(dirBuffer);
					target.addChild("components").setValue(components);
				}
				
				@Override
				public void deserialize(IImmutableVariable source) throws ValueSerializationException
				{
					try
					{
						Integer directionOrdinals[] = source.getChild("directions").getValues(Integer[].class);
						directions = new Direction[directionOrdinals.length];
						
						for(int i = 0; i < directionOrdinals.length; i++)
						{
							if(directionOrdinals[i] < 0 || directionOrdinals[i] > Direction.values().length)
								throw new ValueSerializationException(new IndexOutOfBoundsException("Direction ordinal outside of bounds."));
							
							directions[i] = Direction.values()[directionOrdinals[i]];
						}
						
						components = source.getChild("components").getValues(DefaultSceneModelAnimationComponentDeclaration[].class);
					} catch (NoSuchChildVariableException e) {
						throw new ValueSerializationException(e);
					}
				}
				
				public static final class DefaultSceneModelAnimationComponentDeclaration implements ISerializable
				{
					public String sprite;
					public String animation;
					public Rect3F bounds;
					
					@Override
					public void serialize(IVariable target) throws ValueSerializationException
					{
						target.addChild("sprite").setValue(sprite);
						target.addChild("animation").setValue(animation);
						target.addChild("bounds").setValue(bounds);
					}

					@Override
					public void deserialize(IImmutableVariable source) throws ValueSerializationException
					{
						try {
							sprite = source.getChild("sprite").getValue(String.class);
							animation = source.getChild("animation").getValue(String.class);
							bounds = source.getChild("bounds").getValue(Rect3F.class);
						} catch (NoSuchChildVariableException e) {
							throw new ValueSerializationException(e);
						}
					}
				}
			}
		}
	}
}
