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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.graphics.ui;

import java.awt.Color;
import java.awt.Graphics2D;

import jeva.graphics.AnimationState;
import jeva.graphics.Sprite;
import jeva.joystick.InputManager;
import jeva.joystick.InputManager.InputMouseEvent.MouseButton;

public abstract class Button extends Label
{

	/** The Constant CURSOR_OVER_COLOR. */
	private static final Color CURSOR_OVER_COLOR = new Color(255, 100, 100);

	/** The Constant CURSOR_OFF_COLOR. */
	private static final Color CURSOR_OFF_COLOR = new Color(150, 150, 150);

	/** The m_button sprite. */
	private Sprite m_buttonSprite;

	/**
	 * Instantiates a new button.
	 * 
	 * @param text
	 *            the text
	 */
	public Button(String text)
	{
		super(text, CURSOR_OFF_COLOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#onStyleChanged()
	 */
	@Override
	public final void onStyleChanged()
	{
		super.onStyleChanged();

		if (getStyle() != null)
		{
			m_buttonSprite = getStyle().createButtonSprite();
			m_buttonSprite.setAnimation("off", AnimationState.Play);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#onEnter()
	 */
	@Override
	protected void onEnter()
	{
		setColor(CURSOR_OVER_COLOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#onLeave()
	 */
	@Override
	protected void onLeave()
	{
		setColor(CURSOR_OFF_COLOR);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Label#onMouseEvent(jeva.joystick.InputManager.
	 * InputMouseEvent)
	 */
	@Override
	public void onMouseEvent(InputManager.InputMouseEvent mouseEvent)
	{
		if (mouseEvent.mouseButtonState == false && mouseEvent.mouseButton == MouseButton.Left)
		{
			getStyle().getPressButtonAudio().play();
			onButtonPress();
		}

		super.onMouseEvent(mouseEvent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.graphics.ui.Label#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputManager.InputKeyEvent keyEvent)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Label#render(java.awt.Graphics2D, int, int, float)
	 */
	@Override
	public void render(Graphics2D g, int x, int y, float fScale)
	{
		m_buttonSprite.render(g, x, y, fScale);
		super.render(g, x, y, fScale);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Label#update(int)
	 */
	@Override
	public void update(int deltaTime)
	{
		m_buttonSprite.update(deltaTime);
	}

	/**
	 * On button press.
	 */
	public abstract void onButtonPress();
}
