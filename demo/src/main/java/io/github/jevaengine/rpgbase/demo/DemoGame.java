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
package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.game.DefaultGame;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.ISpriteFactory;
import io.github.jevaengine.graphics.NullGraphic;
import io.github.jevaengine.joystick.IInputSource;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.world.IWorldFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DemoGame extends DefaultGame implements IStateContext
{
	private IRenderable m_cursor;
	private IState m_state;
	
	private Logger m_logger = LoggerFactory.getLogger(DemoGame.class);

	public DemoGame(IInputSource inputSource, IWindowFactory windowFactory, IWorldFactory worldFactory, ISpriteFactory spriteFactory, IAudioClipFactory audioClipFactory, Vector2D resolution)
	{
		super(inputSource, resolution);
	
		try
		{
			m_cursor = spriteFactory.create("ui/style/parpg/cursor/cursor.jsf");
		} catch (AssetConstructionException e)
		{
			m_logger.error("Unable to construct cursor sprite. Reverting to null graphic for cursor.", e);
			m_cursor = new NullGraphic();
		}
		
		m_state = new EntryState(windowFactory, worldFactory, audioClipFactory);
		m_state.enter(this);
	}

	@Override
	public void doLogic(int deltaTime)
	{
		m_state.update(deltaTime);
	}
	
	@Override
	protected IRenderable getCursor()
	{
		return m_cursor;
	}
	
	public void setState(IState state)
	{
		m_state.leave();
		m_state = state;
		state.enter(this);
	}
	
	private static class EntryState implements IState
	{
		private final IWindowFactory m_windowFactory;
		private final IWorldFactory m_worldFactory;
		private final IAudioClipFactory m_audioClipFactory;
		
		private IStateContext m_context;

		public EntryState(IWindowFactory windowFactory, IWorldFactory worldFactory, IAudioClipFactory audioClipFactory)
		{
			m_windowFactory = windowFactory;
			m_worldFactory = worldFactory;
			m_audioClipFactory = audioClipFactory;
		}
		
		@Override
		public void enter(IStateContext context)
		{
			m_context = context;
		}

		@Override
		public void leave() { }

		@Override
		public void update(int deltaTime)
		{
			m_context.setState(new DemoMenu(m_windowFactory, m_worldFactory, m_audioClipFactory));
		}
	}
}
