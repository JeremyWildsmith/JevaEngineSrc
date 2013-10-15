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
package jeva.graphics.ui;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import jeva.graphics.IRenderable;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputMouseEvent;


public class Viewport extends Panel
{

	/** The m_view. */
	private IRenderable m_view;

	
	public Viewport(IRenderable view, int width, int height, boolean renderBackground)
	{
		super(width, height, renderBackground);
		m_view = view;
	}

	
	public Viewport(IRenderable view, int width, int height)
	{
		this(view, width, height, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#render(java.awt.Graphics2D, int, int, float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		super.render(g, x, y, fScale);
		m_view.render(g, x, y, fScale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#getBounds()
	 */
	@Override
	public Rectangle getBounds()
	{
		return super.getBounds();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#onMouseEvent(jeva.joystick.InputManager.
	 * InputMouseEvent)
	 */
	@Override
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.graphics.ui.Panel#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent keyEvent) { }

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Panel#update(int)
	 */
	@Override
	public void update(int deltaTime)
	{

	}

}
