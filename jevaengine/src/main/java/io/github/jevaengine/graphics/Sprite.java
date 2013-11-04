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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import io.github.jevaengine.config.UnknownVariableException;
import io.github.jevaengine.config.Variable;

public final class Sprite implements IRenderable
{

	private static final String VAR_SOURCE = "source";

	private static final String VAR_SCALE = "scale";

	private static final String VAR_ANCHOR = "anchor";

	private static final String VAR_DELAY = "delay";

	private static final String VAR_ANIMATION = "animation";

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

	public Rectangle getBounds()
	{
		return m_currentAnimation.getCurrentFrame().getSourceRect();
	}

	public Point getOrigin()
	{
		return m_currentAnimation.getCurrentFrame().getOrigin();
	}

	public void setAnimation(String animationName, AnimationState state)
	{
		m_currentAnimation = m_animations.get(animationName);

		if (m_currentAnimation == null)
			throw new NoSuchElementException();

		m_currentAnimation.reset();
		m_currentAnimation.setState(state);
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

		Rectangle srcRect = currentFrame.getSourceRect();
		workingGraphics.drawImage(m_srcImage.getImage(), 0, 0, srcRect.width, srcRect.height, srcRect.x, srcRect.y, srcRect.x + srcRect.width, srcRect.y + srcRect.height, null);
	}
}
