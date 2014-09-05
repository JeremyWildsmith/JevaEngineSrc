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
package io.github.jevaengine.game;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.joystick.IInputSource;
import io.github.jevaengine.joystick.IInputSourceProcessor;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.WindowManager;

import java.awt.Graphics2D;

import javax.inject.Inject;

public abstract class DefaultGame implements IGame
{
	private IInputSource m_inputSource;

	private Vector2D m_cursorLocation = new Vector2D();

	private GameInputHandler m_inputHandler = new GameInputHandler();	
	private WindowManager m_windowManager;
	
	@Inject
	public DefaultGame(IInputSource inputSource, Vector2D resolution)
	{
		m_inputSource = inputSource;
		m_windowManager = new WindowManager(resolution);
	}

	@Override
	public final void render(IRenderer r)
	{
		r.render(new IRenderable() {
			@Override
			public void render(Graphics2D g, int x, int y, float scale) {
				getWindowManager().render(g, x, y, 1.0F);
				getCursor().render(g, m_cursorLocation.x, m_cursorLocation.y, 1.0F);				
			}
		});
	}

	@Override
	public final void update(int deltaTime)
	{
		m_inputSource.process(m_inputHandler);
		m_windowManager.update(deltaTime);
		
		doLogic(deltaTime);
	}
	
	@Override
	public final WindowManager getWindowManager()
	{
		return m_windowManager;
	}
	
	protected abstract void doLogic(int deltaTime);
	protected abstract IRenderable getCursor();
	
	private class GameInputHandler implements IInputSourceProcessor
	{
		@Override
		public void mouseMoved(InputMouseEvent e)
		{
			m_cursorLocation = new Vector2D(e.location);

			getWindowManager().onMouseEvent(e);
		}

		@Override
		public void mouseClicked(InputMouseEvent e)
		{
			getWindowManager().onMouseEvent(e);
		}

		@Override
		public void keyUp(InputKeyEvent e)
		{
			getWindowManager().onKeyEvent(e);
		}

		@Override
		public void keyDown(InputKeyEvent e)
		{
			getWindowManager().onKeyEvent(e);
		}

		@Override
		public void keyTyped(InputKeyEvent e)
		{
			getWindowManager().onKeyEvent(e);
		}

		@Override
		public void mouseWheelMoved(InputMouseEvent e)
		{
			getWindowManager().onMouseEvent(e);
		}

		@Override
		public void mouseLeft(InputMouseEvent e) { }

		@Override
		public void mouseEntered(InputMouseEvent e) { }

		@Override
		public void mouseButtonStateChanged(InputMouseEvent e)
		{
			getWindowManager().onMouseEvent(e);
		}
	}
}
