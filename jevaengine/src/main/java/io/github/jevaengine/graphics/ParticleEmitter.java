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
package io.github.jevaengine.graphics;

import java.awt.Graphics2D;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration;
import io.github.jevaengine.math.Vector2F;

public final class ParticleEmitter implements IRenderable
{
	private static final int MAX_SPRITES = 25;

	private Sprite[] m_spriteMaps;

	private Sprite[] m_particleSprites;

	private Vector2F m_acceleration;

	private int m_particleCount;

	private int m_particleLife;

	private float m_fVariation;

	private Vector2F m_velocity;

	private float[] m_particleBuffer;

	private boolean m_isEmitting;

	public ParticleEmitter(Sprite[] spriteMaps, Vector2F acceleration, Vector2F velocity, int particleCount, int particleLife, float fVariation)
	{
		m_spriteMaps = spriteMaps;
		m_acceleration = acceleration;
		m_velocity = velocity;
		m_particleCount = particleCount;
		m_particleLife = particleLife;
		m_fVariation = fVariation;
		m_particleBuffer = new float[m_particleCount * ParticleOffset.Size.ordinal()];
		m_particleSprites = new Sprite[MAX_SPRITES];

		m_isEmitting = false;
	}

	public static ParticleEmitter create(ParticleEmitterDeclaration decl)
	{
		Sprite[] spriteMaps = new Sprite[decl.sprites.length];

		for (int i = 0; i < decl.sprites.length; i++)
			spriteMaps[i] = Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration(decl.sprites[i]).getValue(SpriteDeclaration.class));

		return new ParticleEmitter(spriteMaps, 
									decl.acceleration, 
									decl.velocity, 
									Math.max(1, decl.count), 
									Math.max(100, decl.life), 
									decl.variation);
	}

	public void update(int deltaTime)
	{
		for (int i = 0; i < m_particleCount; i++)
		{
			int particle = (i % m_particleCount);
			int base = particle * ParticleOffset.Size.offset;

			if (m_particleBuffer[base + ParticleOffset.Life.offset] <= 0 && m_isEmitting)
			{
				m_particleBuffer[base + ParticleOffset.Life.offset] = m_particleLife * (float) Math.random();

				m_particleBuffer[base + ParticleOffset.LocationX.offset] = 0;
				m_particleBuffer[base + ParticleOffset.LocationY.offset] = 0;

				Vector2F velocity = m_velocity.multiply(1.0F + m_fVariation * (Math.random() > 0.5 ? -1 : 1)).rotate(m_fVariation * (float) Math.random() * (float) Math.PI * 2);

				m_particleBuffer[base + ParticleOffset.VelocityX.offset] = velocity.x;
				m_particleBuffer[base + ParticleOffset.VelocityY.offset] = velocity.y;

				Vector2F acceleration = m_acceleration.multiply(1.0F + m_fVariation * (Math.random() > 0.5 ? -1 : 1)).rotate(m_fVariation * (float) Math.random() * (float) Math.PI * 2);

				m_particleBuffer[base + ParticleOffset.AccelerationX.offset] = acceleration.x;
				m_particleBuffer[base + ParticleOffset.AccelerationY.offset] = acceleration.y;

				if (m_particleSprites[particle % m_particleSprites.length] == null)
				{
					m_particleSprites[particle % m_particleSprites.length] = new Sprite(m_spriteMaps[((int) (Math.random() * 100) % m_spriteMaps.length)]);
					m_particleSprites[particle % m_particleSprites.length].setAnimation("idle", AnimationState.PlayToEnd);
				}
			}

			m_particleBuffer[base + ParticleOffset.LocationX.offset] += m_particleBuffer[base + ParticleOffset.VelocityX.offset] * (float) deltaTime / 1000.0F;
			m_particleBuffer[base + ParticleOffset.LocationY.offset] += m_particleBuffer[base + ParticleOffset.VelocityY.offset] * (float) deltaTime / 1000.0F;

			m_particleBuffer[base + ParticleOffset.VelocityX.offset] += m_particleBuffer[base + ParticleOffset.AccelerationX.offset] * (float) deltaTime / 1000.0F;
			m_particleBuffer[base + ParticleOffset.VelocityY.offset] += m_particleBuffer[base + ParticleOffset.AccelerationY.offset] * (float) deltaTime / 1000.0F;

			m_particleBuffer[base + ParticleOffset.Life.offset] -= deltaTime;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.IRenderable#render(java.awt.Graphics2D, int, int,
	 * float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		for (int i = 0; i < m_particleCount; i++)
		{
			int base = i * ParticleOffset.Size.offset;

			if (m_particleSprites[i % m_particleSprites.length] != null && m_particleBuffer[base + ParticleOffset.Life.offset] > 0)
				m_particleSprites[i % m_particleSprites.length].render(g, 
					x + Math.round(m_particleBuffer[base + ParticleOffset.LocationX.offset]), 
					y + Math.round(m_particleBuffer[base + ParticleOffset.LocationY.offset]), 
					fScale * (float) m_particleBuffer[base + ParticleOffset.Life.offset] / (float) m_particleLife);
		}
	}

	public void setEmit(boolean emit)
	{
		m_isEmitting = emit;
	}
	
	private static enum ParticleOffset
	{

		LocationX(0),

		LocationY(1),

		VelocityX(2),

		VelocityY(3),

		AccelerationX(4),

		AccelerationY(5),

		Life(6),

		Size(7);

		public int offset;

		ParticleOffset(int _offset)
		{
			offset = _offset;
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
		public void serialize(IVariable target)
		{
			target.addChild("count").setValue(this.count);
			target.addChild("life").setValue(this.life);
			target.addChild("velocity").setValue(this.velocity);
			target.addChild("acceleration").setValue(this.acceleration);
			target.addChild("variation").setValue(this.variation);
			target.addChild("sprites").setValue(this.sprites);
		}

		@Override
		public void deserialize(IImmutableVariable source)
		{
			this.count = source.getChild("count").getValue(Integer.class);
			this.life = source.getChild("life").getValue(Integer.class);
			this.velocity = source.getChild("velocity").getValue(Vector2F.class);
			this.acceleration = source.getChild("acceleration").getValue(Vector2F.class);
			this.variation = source.getChild("variation").getValue(Double.class).floatValue();
			this.sprites = source.getChild("sprites").getValues(String[].class);
		}
		
		
	}
}
