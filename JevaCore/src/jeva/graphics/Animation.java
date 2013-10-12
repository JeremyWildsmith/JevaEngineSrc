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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public final class Animation
{

	/** The m_frames. */
	private List<Frame> m_frames;

	/** The m_cur index. */
	private int m_curIndex;

	/** The m_elapsed time. */
	private int m_elapsedTime;

	/** The m_state. */
	private AnimationState m_state;

	/**
	 * Instantiates a new animation.
	 * 
	 * @param src
	 *            the src
	 */
	public Animation(Animation src)
	{
		m_curIndex = 0;
		m_elapsedTime = 0;
		m_frames = src.m_frames;
		m_state = AnimationState.Stop;
	}

	/**
	 * Instantiates a new animation.
	 */
	public Animation()
	{
		m_curIndex = 0;
		m_elapsedTime = 0;
		m_frames = new ArrayList<Frame>();
		m_state = AnimationState.Stop;
	}

	/**
	 * Instantiates a new animation.
	 * 
	 * @param frames
	 *            the frames
	 */
	public Animation(Frame... frames)
	{
		m_elapsedTime = 0;
		m_frames = new ArrayList<Frame>();
		m_state = AnimationState.Stop;

		m_frames.addAll(Arrays.asList(frames));
	}

	/**
	 * Reset.
	 */
	public void reset()
	{
		m_curIndex = 0;
	}

	/**
	 * Adds the frame.
	 * 
	 * @param frame
	 *            the frame
	 */
	public void addFrame(Frame frame)
	{
		m_frames.add(frame);
	}

	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(AnimationState state)
	{
		m_state = state;
	}

	/**
	 * Update.
	 * 
	 * @param deltaTime
	 *            the delta time
	 */
	public void update(int deltaTime)
	{
		m_elapsedTime += deltaTime;

		if (m_frames.isEmpty() || m_state == AnimationState.Stop || m_elapsedTime < getCurrentFrame().getDelay())
		{
			return;
		}

		while (m_elapsedTime > getCurrentFrame().getDelay())
		{
			m_elapsedTime -= getCurrentFrame().getDelay();

			switch (m_state)
			{
			case Stop:
				break;
			case PlayToEnd:
				if (m_curIndex == m_frames.size() - 1)
				{
					m_state = AnimationState.Stop;
					break;
				}
			case Play:
				m_curIndex = (m_curIndex + 1) % m_frames.size();
				break;
			default:
				throw new UnknownAnimationStateException(m_state);
			}
		}
	}

	/**
	 * Gets the current frame.
	 * 
	 * @return the current frame
	 */
	public Frame getCurrentFrame()
	{
		if (m_curIndex >= m_frames.size())
		{
			throw new NoSuchElementException();
		}

		return m_frames.get(m_curIndex);
	}
}
