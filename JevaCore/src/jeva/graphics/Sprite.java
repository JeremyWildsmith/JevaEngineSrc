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
package jeva.graphics;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import jeva.config.UnknownVariableException;
import jeva.config.Variable;

/**
 * The Class Sprite.
 */
public final class Sprite implements IRenderable
{

	/** The Constant VAR_SOURCE. */
	private static final String VAR_SOURCE = "source";

	/** The Constant VAR_SCALE. */
	private static final String VAR_SCALE = "scale";

	/** The Constant VAR_ANCHOR. */
	private static final String VAR_ANCHOR = "anchor";

	/** The Constant VAR_DELAY. */
	private static final String VAR_DELAY = "delay";

	/** The Constant VAR_ANIMATION. */
	private static final String VAR_ANIMATION = "animation";

	/** The m_animations. */
	private HashMap<String, Animation> m_animations;

	/** The m_current animation. */
	private Animation m_currentAnimation;

	/** The m_src image. */
	private Graphic m_srcImage;

	/** The m_f natural scale. */
	private float m_fNaturalScale;

	/**
	 * Instantiates a new sprite.
	 * 
	 * @param src
	 *            the src
	 */
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

	/**
	 * Instantiates a new sprite.
	 * 
	 * @param srcImage
	 *            the src image
	 * @param fNaturalScale
	 *            the f natural scale
	 */
	public Sprite(Graphic srcImage, float fNaturalScale)
	{
		m_srcImage = srcImage;
		m_currentAnimation = null;
		m_animations = new HashMap<String, Animation>();

		m_fNaturalScale = fNaturalScale;
	}

	/**
	 * Creates the.
	 * 
	 * @param root
	 *            the root
	 * @return the sprite
	 */
	public static Sprite create(Variable root)
	{
		try
		{
			Variable source = root.getVariable(VAR_SOURCE);
			Variable scale = root.getVariable(VAR_SCALE);

			Graphic srcImage = Graphic.create(source.getValue().getString());

			Sprite sprite = new Sprite(srcImage, scale.getValue().getFloat());

			for (Variable anim : root.getVariable(VAR_ANIMATION))
			{
				Animation animBuffer = new Animation();

				Variable[] anchors = root.getVariable(VAR_ANCHOR).getVariable(anim.getName()).getVariableArray();
				Variable[] delays = root.getVariable(VAR_DELAY).getVariable(anim.getName()).getVariableArray();
				Variable[] frames = anim.getVariableArray();

				for (int i = 0; i < frames.length; i++)
				{
					animBuffer.addFrame(new Frame(frames[i].getValue().getRectangle(), delays[i].getValue().getInt(), anchors[i].getValue().getPoint()));
				}

				sprite.addAnimation(anim.getName(), animBuffer);
			}

			return sprite;

		} catch (UnknownVariableException ex)
		{
			throw new RuntimeException(ex);
		} catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Gets the bounds.
	 * 
	 * @return the bounds
	 */
	public Rectangle getBounds()
	{
		return m_currentAnimation.getCurrentFrame().getSourceRect();
	}

	/**
	 * Gets the origin.
	 * 
	 * @return the origin
	 */
	public Point getOrigin()
	{
		return m_currentAnimation.getCurrentFrame().getOrigin();
	}

	/**
	 * Sets the animation.
	 * 
	 * @param animationName
	 *            the animation name
	 * @param state
	 *            the state
	 */
	public void setAnimation(String animationName, AnimationState state)
	{
		m_currentAnimation = m_animations.get(animationName);

		if (m_currentAnimation == null)
			throw new NoSuchElementException();

		m_currentAnimation.reset();
		m_currentAnimation.setState(state);
	}

	/**
	 * Adds the animation.
	 * 
	 * @param name
	 *            the name
	 * @param anim
	 *            the anim
	 */
	public void addAnimation(String name, Animation anim)
	{
		m_animations.put(name, anim);
	}

	/**
	 * Update.
	 * 
	 * @param deltaTime
	 *            the delta time
	 */
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
	 * @see jeva.graphics.IRenderable#render(java.awt.Graphics2D, int, int,
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

		Rectangle srcRect = currentFrame.getSourceRect();
		workingGraphics.drawImage(m_srcImage.getImage(), 0, 0, srcRect.width, srcRect.height, srcRect.x, srcRect.y, srcRect.x + srcRect.width, srcRect.y + srcRect.height, null);
	}
}
