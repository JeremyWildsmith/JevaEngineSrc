package io.github.jevaengine.graphics;

import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.graphics.DefaultSpriteFactory.SpriteDeclaration.AnimationDeclaration;
import io.github.jevaengine.graphics.DefaultSpriteFactory.SpriteDeclaration.FrameDeclaration;
import io.github.jevaengine.graphics.IGraphicFactory.GraphicConstructionException;
import io.github.jevaengine.graphics.Sprite.NoSuchSpriteAnimation;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.util.ThreadSafe;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DefaultSpriteFactory implements ISpriteFactory
{
	private final IConfigurationFactory m_configurationFactory;
	private final IGraphicFactory m_graphicFactory;
	
	@Inject
	public DefaultSpriteFactory(IConfigurationFactory configurationFactory, IGraphicFactory graphicFactory)
	{
		m_configurationFactory = configurationFactory;
		m_graphicFactory = graphicFactory;
	}
	
	@Override
	@ThreadSafe
	public Sprite create(String name) throws SpriteConstructionException
	{
		try
		{
			SpriteDeclaration spriteDecl = m_configurationFactory.create(name).getValue(SpriteDeclaration.class);
			
			IImmutableGraphic srcImage = m_graphicFactory.create(spriteDecl.texture);
	
			Sprite sprite = new Sprite(srcImage, spriteDecl.scale);
	
			for (AnimationDeclaration anim : spriteDecl.animations)
			{
				Animation animBuffer = new Animation();
	
				for(FrameDeclaration frame : anim.frames)
					animBuffer.addFrame(new Frame(frame.region, frame.delay, frame.anchor, frame.event));
	
				sprite.addAnimation(anim.name, animBuffer);
			}
	
			if(spriteDecl.defaultAnimation != null)
				sprite.setAnimation(spriteDecl.defaultAnimation, AnimationState.Play);
			
			return sprite;
		} catch (ValueSerializationException |
				 ConfigurationConstructionException |
				 GraphicConstructionException |
				 NoSuchSpriteAnimation e)
		{
			throw new SpriteConstructionException(name, e);
		}
	}
	
	public static class SpriteDeclaration implements ISerializable
	{
		public String defaultAnimation;
		public String texture;
		public float scale;
		public AnimationDeclaration[] animations;
		
		public SpriteDeclaration() { }
		
		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			if(defaultAnimation != null)
				target.addChild("defaultAnimation").setValue(defaultAnimation);
			
			target.addChild("texture").setValue(this.texture);
			target.addChild("scale").setValue((double)this.scale);
			target.addChild("animations").setValue(animations);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				if(source.childExists("defaultAnimation"))
					this.defaultAnimation = source.getChild("defaultAnimation").getValue(String.class);
				
				this.texture = source.getChild("texture").getValue(String.class);
				this.scale = source.getChild("scale").getValue(Double.class).floatValue();
				this.animations = source.getChild("animations").getValues(AnimationDeclaration[].class);
			} catch(NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}
		
		public static class AnimationDeclaration implements ISerializable
		{
			public String name;
			public FrameDeclaration[] frames;
			
			public AnimationDeclaration() { }

			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				target.addChild("name").setValue(this.name);
				target.addChild("frames").setValue(this.frames);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					this.name = source.getChild("name").getValue(String.class);
					this.frames = source.getChild("frames").getValues(FrameDeclaration[].class);
				} catch(NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
		
		public static class FrameDeclaration implements ISerializable
		{
			public Rect2D region;
			public Vector2D anchor;
			public int delay;
			public String event;
			
			public FrameDeclaration() { }

			@Override
			public void serialize(IVariable target) throws ValueSerializationException
			{
				if(event != null)
					target.addChild("event").setValue(this.event);
				
				target.addChild("region").setValue(this.region);
				target.addChild("anchor").setValue(this.anchor);
				target.addChild("delay").setValue(this.delay);
			}

			@Override
			public void deserialize(IImmutableVariable source) throws ValueSerializationException
			{
				try
				{
					if(source.childExists("event"))
						this.event = source.getChild("event").getValue(String.class);
					
					this.region = source.getChild("region").getValue(Rect2D.class);
					this.anchor = source.getChild("anchor").getValue(Vector2D.class);
					this.delay = source.getChild("delay").getValue(Integer.class);
				} catch(NoSuchChildVariableException e)
				{
					throw new ValueSerializationException(e);
				}
			}
		}
	}
}
