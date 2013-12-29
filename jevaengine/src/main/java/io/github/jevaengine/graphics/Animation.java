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

import io.github.jevaengine.util.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

public final class Animation
{

	private List<Frame> m_frames;

	private int m_curIndex;

	private int m_elapsedTime;

	private AnimationState m_state;

	private boolean m_playWrapBackwards = false;
	
	private @Nullable Runnable m_eventHandler;
	
	public Animation(Animation src)
	{
		m_curIndex = 0;
		m_elapsedTime = 0;
		m_frames = src.m_frames;
		m_state = AnimationState.Stop;
	}

	public Animation()
	{
		m_curIndex = 0;
		m_elapsedTime = 0;
		m_frames = new ArrayList<Frame>();
		m_state = AnimationState.Stop;
	}

	public Animation(Frame... frames)
	{
		m_elapsedTime = 0;
		m_frames = new ArrayList<Frame>();
		m_state = AnimationState.Stop;

		m_frames.addAll(Arrays.asList(frames));
	}
	
	private void triggerEvent()
	{
		if(m_eventHandler != null)
			m_eventHandler.run();
	}

	public void reset()
	{
		m_curIndex = 0;
		m_playWrapBackwards = false;
	}

	public void addFrame(Frame frame)
	{
		m_frames.add(frame);
	}

	public void setState(AnimationState state)
	{
		setState(state, null);
	}
	
	public void setState(AnimationState state, @Nullable Runnable eventHandler)
	{
		m_state = state;
		m_playWrapBackwards = false;
		m_eventHandler = eventHandler;
	}

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
						triggerEvent();
						break;
					}
				case Play:
					m_curIndex = (m_curIndex + 1) % m_frames.size();
					break;
				case PlayWrap:
					if(m_curIndex == m_frames.size() - 1)
					{
						m_playWrapBackwards = true;
						triggerEvent();
					}
					else if(m_curIndex == 0)
					{
						m_playWrapBackwards = false;
						triggerEvent();
					}
					
					if(m_playWrapBackwards)
						m_curIndex = Math.max(0, m_curIndex - 1);
					else
						m_curIndex = (m_curIndex + 1) % m_frames.size();
					
					break;
				default:
					throw new UnknownAnimationStateException(m_state);
			}
		}
	}

	public Frame getCurrentFrame()
	{
		if (m_curIndex >= m_frames.size())
		{
			throw new NoSuchElementException();
		}

		return m_frames.get(m_curIndex);
	}
}
