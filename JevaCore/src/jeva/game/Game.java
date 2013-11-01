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
package jeva.game;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;

import jeva.Core;
import jeva.graphics.Sprite;
import jeva.graphics.ui.*;
import jeva.joystick.*;
import jeva.math.Vector2D;


public abstract class Game implements IInputDeviceListener
{

	/** The m_input man. */
	private InputManager m_inputMan;

	/** The m_cursor. */
	private Sprite m_cursor;

	/** The cursor location. */
	private Vector2D m_cursorLocation = new Vector2D();

	/** The m_render buffer. */
	private VolatileImage m_renderBuffer;

	/** The m_buffer stratedgy. */
	private BufferStrategy m_bufferStratedgy;

	/** The m_gfx config. */
	private GraphicsConfiguration m_gfxConfig;

	/** The m_target width. */
	private int m_targetWidth;

	/** The m_target height. */
	private int m_targetHeight;

	/** The m_render width. */
	private int m_renderWidth;

	/** The m_render height. */
	private int m_renderHeight;

	
	public Game() { }

	public final void init(Frame target, int resolutionX, int resolutionY)
	{
		target.createBufferStrategy(2);

		m_renderWidth = resolutionX;
		m_renderHeight = resolutionY;

		m_gfxConfig = target.getGraphicsConfiguration();
		m_targetWidth = target.getWidth();
		m_targetHeight = target.getHeight();

		m_bufferStratedgy = target.getBufferStrategy();

		m_renderBuffer = m_gfxConfig.createCompatibleVolatileImage(m_renderWidth, m_renderHeight);

		m_inputMan = InputManager.create(target);

		startup();

		m_cursor = getCursor();
	}
	
	public final Vector2D getResolution()
	{
		return new Vector2D(m_renderWidth, m_renderHeight);
	}
	
	public final void render()
	{
		do
		{
			if (m_renderBuffer.validate(m_gfxConfig) == VolatileImage.IMAGE_INCOMPATIBLE)
			{
				m_renderBuffer.flush();
				m_renderBuffer = m_gfxConfig.createCompatibleVolatileImage(m_renderWidth, m_renderHeight);
			}

			Graphics2D g = (Graphics2D) m_renderBuffer.getGraphics();

			g.setColor(Color.black);
			g.fillRect(0, 0, m_targetWidth, m_targetHeight);

			Core.getService(IWindowManager.class).render(g, 0, 0, 1.0F);

			m_cursor.render(g, m_cursorLocation.x, m_cursorLocation.y, 1.0F);

			g.dispose();

			g = (Graphics2D) m_bufferStratedgy.getDrawGraphics();
			g.drawImage(m_renderBuffer, 0, 0, m_targetWidth, m_targetHeight, null);
			g.dispose();

			m_bufferStratedgy.show();

		} while (m_bufferStratedgy.contentsLost() || m_renderBuffer.contentsLost());

		Toolkit.getDefaultToolkit().sync();
	}

	
	public void update(int deltaTime)
	{
		m_inputMan.process(this);

		Core.getService(IWindowManager.class).update(deltaTime);
	}

	// Control Input
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseMoved(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseMoved(InputManager.InputMouseEvent e)
	{
		m_cursorLocation = new Vector2D(e.location);

		Core.getService(IWindowManager.class).onMouseEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseClicked(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseClicked(InputManager.InputMouseEvent e)
	{
		Core.getService(IWindowManager.class).onMouseEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.joystick.IInputDeviceListener#keyUp(jeva.joystick.InputManager.
	 * InputKeyEvent)
	 */
	@Override
	public void keyUp(InputManager.InputKeyEvent e)
	{
		Core.getService(IWindowManager.class).onKeyEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#keyDown(jeva.joystick.InputManager
	 * .InputKeyEvent)
	 */
	@Override
	public void keyDown(InputManager.InputKeyEvent e)
	{
		Core.getService(IWindowManager.class).onKeyEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#keyTyped(jeva.joystick.InputManager
	 * .InputKeyEvent)
	 */
	public void keyTyped(InputManager.InputKeyEvent e)
	{
		Core.getService(IWindowManager.class).onKeyEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseWheelMoved(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseWheelMoved(InputManager.InputMouseEvent e)
	{
		Core.getService(IWindowManager.class).onMouseEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseLeft(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseLeft(InputManager.InputMouseEvent e) { }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseEntered(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseEntered(InputManager.InputMouseEvent e) { }

	
	public abstract UIStyle getGameStyle();

	
	public abstract IGameScriptProvider getScriptBridge();

	
	protected abstract void startup();

	
	protected abstract Sprite getCursor();
}
