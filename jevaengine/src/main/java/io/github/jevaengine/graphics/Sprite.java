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

import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration.AnimationDeclaration;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration.FrameDeclaration;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

public final class Sprite implements IRenderable
{
	private HashMap<String, Animation> m_animations;

	private Animation m_currentAnimation;

	private Graphic m_srcImage;

	private float m_fNaturalScale;

	public Sprite(Sprite src)
	{
		m_srcImage = src.m_srcImage;
		m_currentAnimation = null;
		m_animations = new HashMap<String, Animation>();
		m_fNaturalScale = src.m_fNaturalScale;

		for (Entry<String, Animation> entry : src.m_animations.entrySet())
		{
			m_animations.put(entry.getKey(), new Animation(entry.getValue()));
		}
	}

	public Sprite(Graphic srcImage, float fNaturalScale)
	{
		m_srcImage = srcImage;
		m_currentAnimation = null;
		m_animations = new HashMap<String, Animation>();

		m_fNaturalScale = fNaturalScale;
	}
	
	public static Sprite create(IVariable root)
	{
		SpriteDeclaration spriteDecl = root.getValue(SpriteDeclaration.class);
			
		Graphic srcImage = Graphic.create(spriteDecl.texture);

		Sprite sprite = new Sprite(srcImage, spriteDecl.scale);

		for (AnimationDeclaration anim : spriteDecl.animations)
		{
			Animation animBuffer = new Animation();

			for(FrameDeclaration frame : anim.frames)
			{
				animBuffer.addFrame(new Frame(frame.region, frame.delay, frame.anchor));
			}

			sprite.addAnimation(anim.name, animBuffer);
		}

		return sprite;
	}

	public Rect2D getBounds()
	{
		Rect2D source = m_currentAnimation.getCurrentFrame().getSourceRect();
	
		return new Rect2D(0, 0, source.width, source.height).difference(getOrigin());
	}
	
	public Rect2F getBounds(float scale)
	{
		Rect2D bounds = getBounds();
		
		return new Rect2F(0, 0, bounds.width, bounds.height).difference(getOrigin(scale));
	}

	public Vector2D getOrigin()
	{
		return m_currentAnimation.getCurrentFrame().getOrigin();
	}
	
	public Vector2F getOrigin(float scale)
	{
		Vector2D srcOrigin = getOrigin();
		
		return new Vector2F((float)srcOrigin.x * scale, (float)srcOrigin.y * scale);
	}

	public String[] getAnimations()
	{
		Set<String> keys = m_animations.keySet();
		
		return keys.toArray(new String[keys.size()]);
	}

	public void setAnimation(String animationName, AnimationState state)
	{
		setAnimation(animationName, state, null);
	}
	
	public void setAnimation(String animationName, AnimationState state, @Nullable Runnable animationEventHandler)
	{
		m_currentAnimation = m_animations.get(animationName);

		if (m_currentAnimation == null)
			throw new NoSuchElementException();

		m_currentAnimation.reset();
		m_currentAnimation.setState(state, animationEventHandler);
	}

	public void addAnimation(String name, Animation anim)
	{
		m_animations.put(name, anim);
	}

	public void update(int deltaTime)
	{
		if (m_currentAnimation != null)
		{
			m_currentAnimation.update(deltaTime);
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
		if (m_currentAnimation == null)
		{
			throw new NoSuchElementException();
		}

		Frame currentFrame = m_currentAnimation.getCurrentFrame();

		int destX = x - Math.round(currentFrame.getOrigin().x * fScale * m_fNaturalScale);
		int destY = y - Math.round(currentFrame.getOrigin().y * fScale * m_fNaturalScale);

		AffineTransform trans = new AffineTransform();
		trans.translate(destX, destY);
		trans.scale(fScale * m_fNaturalScale, fScale * m_fNaturalScale);

		Graphics2D workingGraphics = (Graphics2D) g.create();
		workingGraphics.transform(trans);

		Rect2D srcRect = currentFrame.getSourceRect();
	
		m_srcImage.render(workingGraphics, 0, 0, srcRect.width, srcRect.height, srcRect.x, srcRect.y, srcRect.width, srcRect.height);
		workingGraphics.dispose();
	}
	
	public boolean testPick(int x, int y, float scale)
	{
		if(m_currentAnimation == null)
			return false;
		
		Rect2D bounds = m_currentAnimation.getCurrentFrame().getSourceRect();
		
		int xTest = Math.round(x * (1.0F / (m_fNaturalScale * scale))) + m_currentAnimation.getCurrentFrame().getOrigin().x;
		int yTest = Math.round(y * (1.0F / (m_fNaturalScale * scale))) + m_currentAnimation.getCurrentFrame().getOrigin().y;
		
		if(xTest < 0 || yTest < 0 || xTest > bounds.width || yTest > bounds.height)
			return false;
		
		xTest += bounds.x;
		yTest += bounds.y;
		
		return  m_srcImage.pickTest(xTest, yTest);
	
	}
	
	public static class SpriteDeclaration implements ISerializable
	{
		public String texture;
		public float scale;
		public AnimationDeclaration[] animations;
		public SpriteDeclaration() { }
		
		@Override
		public void serialize(IVariable target)
		{
			target.addChild("texture").setValue(this.texture);
			target.addChild("scale").setValue((double)this.scale);
			target.addChild("animations").setValue(animations);
		}

		@Override
		public void deserialize(IVariable source)
		{
			this.texture = source.getChild("texture").getValue(String.class);
			this.scale = source.getChild("scale").getValue(Double.class).floatValue();
			this.animations = source.getChild("animations").getValues(AnimationDeclaration[].class);
		}
		
		public static class AnimationDeclaration implements ISerializable
		{
			public String name;
			public FrameDeclaration[] frames;
			
			public AnimationDeclaration() { }

			@Override
			public void serialize(IVariable target)
			{
				target.addChild("name").setValue(this.name);
				target.addChild("frames").setValue(this.frames);
			}

			@Override
			public void deserialize(IVariable source)
			{
				this.name = source.getChild("name").getValue(String.class);
				this.frames = source.getChild("frames").getValues(FrameDeclaration[].class);
			}
		}
		
		public static class FrameDeclaration implements ISerializable
		{
			public Rect2D region;
			public Vector2D anchor;
			public int delay;
			
			public FrameDeclaration() { }

			@Override
			public void serialize(IVariable target)
			{
				target.addChild("region").setValue(this.region);
				target.addChild("anchor").setValue(this.anchor);
				target.addChild("delay").setValue(this.delay);
			}

			@Override
			public void deserialize(IVariable source)
			{
				this.region = source.getChild("region").getValue(Rect2D.class);
				this.anchor = source.getChild("anchor").getValue(Vector2D.class);
				this.delay = source.getChild("delay").getValue(Integer.class);
			}
		}
	}
}
