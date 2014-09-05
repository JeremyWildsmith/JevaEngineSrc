package io.github.jevaengine.graphics;

import io.github.jevaengine.config.IConfigurationFactory;
import io.github.jevaengine.config.IConfigurationFactory.ConfigurationConstructionException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.graphics.ISpriteFactory.SpriteConstructionException;
import io.github.jevaengine.math.Vector2F;

public final class DefaultParticleEmitterFactory implements IParticleEmitterFactory
{
	private final ISpriteFactory m_spriteFactory;
	private final IConfigurationFactory m_configurationFactory;
	
	public DefaultParticleEmitterFactory(ISpriteFactory spriteFactory, IConfigurationFactory configurationFactory)
	{
		m_spriteFactory = spriteFactory;
		m_configurationFactory = configurationFactory;
	}
	
	@Override
	public ParticleEmitter create(String name) throws ParticleEmitterConstructionException
	{
		try
		{
			ParticleEmitterDeclaration decl = m_configurationFactory.create(name).getValue(ParticleEmitterDeclaration.class);
			
			Sprite[] spriteMaps = new Sprite[decl.sprites.length];
	
			for (int i = 0; i < decl.sprites.length; i++)
				spriteMaps[i] = m_spriteFactory.create(decl.sprites[i]);
	
			return new ParticleEmitter(spriteMaps, 
										decl.acceleration, 
										decl.velocity, 
										Math.max(1, decl.count), 
										Math.max(100, decl.life), 
										decl.variation);
		} catch(ValueSerializationException |
				SpriteConstructionException |
				ConfigurationConstructionException e)
		{
			throw new ParticleEmitterConstructionException(name, e);
		}
	}

	public static class ParticleEmitterDeclaration implements ISerializable
	{
		public int count;
		public int life;
		public Vector2F velocity;
		public Vector2F acceleration;
		public float variation;
		public String[] sprites;

		public ParticleEmitterDeclaration() { }
		
		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("count").setValue(this.count);
			target.addChild("life").setValue(this.life);
			target.addChild("velocity").setValue(this.velocity);
			target.addChild("acceleration").setValue(this.acceleration);
			target.addChild("variation").setValue(this.variation);
			target.addChild("sprites").setValue(this.sprites);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				this.count = source.getChild("count").getValue(Integer.class);
				this.life = source.getChild("life").getValue(Integer.class);
				this.velocity = source.getChild("velocity").getValue(Vector2F.class);
				this.acceleration = source.getChild("acceleration").getValue(Vector2F.class);
				this.variation = source.getChild("variation").getValue(Double.class).floatValue();
				this.sprites = source.getChild("sprites").getValues(String[].class);
			} catch(NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}
	}
}
