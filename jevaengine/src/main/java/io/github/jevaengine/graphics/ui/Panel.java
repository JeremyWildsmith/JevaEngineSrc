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
package io.github.jevaengine.graphics.ui;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.EventType;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;

public abstract class Panel extends Control
{
	private boolean m_renderBackground;

	private Sprite m_frameFill;

	private Sprite m_frameLeft;

	private Sprite m_frameRight;

	private Sprite m_frameTop;

	private Sprite m_frameBottom;

	private Sprite m_frameTopLeft;

	private Sprite m_frameTopRight;

	private Sprite m_frameBottomLeft;

	private Sprite m_frameBottomRight;

	private int m_width;

	private int m_height;

	private Control m_activeControl;

	private Control m_lastOver;

	private StaticSet<Control> m_controls;

	public Panel(int width, int height, boolean renderBackground)
	{
		m_width = width;
		m_height = height;
		m_renderBackground = renderBackground;

		m_controls = new StaticSet<Control>();
	}

	public Panel(int width, int height)
	{
		this(width, height, true);
	}

	public void addControl(Control control)
	{
		addControl(control, null);
	}

	public void addControl(Control control, @Nullable Vector2D location)
	{
		if (!m_controls.contains(control))
		{
			m_controls.add(control);
			control.setParent(this);

			if (location != null)
				control.setLocation(location);
		} else
		{
			// Move to top.
			m_controls.remove(control);
			m_controls.add(control);

			if (location != null)
				control.setLocation(location);
		}
	}

	public void removeControl(Control control)
	{
		if (m_controls.contains(control))
		{
			control.setParent(null);
			m_controls.remove(control);
		}
	}

	public void clearControls()
	{
		m_activeControl = null;

		for (Control ctrl : m_controls)
			removeControl(ctrl);
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
		if (m_renderBackground)
		{
			// Render upper border
			m_frameTopLeft.render(g, x, y, fScale);

			int offsetX;

			for (offsetX = m_frameTopLeft.getBounds().width; offsetX < m_width - m_frameTopRight.getBounds().width; offsetX += m_frameTop.getBounds().width)
			{
				m_frameTop.render(g, x + offsetX, y, fScale);
			}
			m_frameTopRight.render(g, x + offsetX, y, fScale);

			offsetX += m_frameTopRight.getBounds().width;

			int offsetY = m_frameTop.getBounds().height;
			// Render fill and left\right border
			for (; offsetY < m_height - m_frameBottom.getBounds().height; offsetY += m_frameFill.getBounds().width)
			{
				m_frameLeft.render(g, x, offsetY + y, fScale);

				for (offsetX = m_frameLeft.getBounds().width; offsetX < m_width - m_frameRight.getBounds().width; offsetX += m_frameFill.getBounds().width)
				{
					m_frameFill.render(g, x + offsetX, y + offsetY, fScale);
				}

				m_frameRight.render(g, x + offsetX, offsetY + y, fScale);
			}

			// Render lower border

			offsetX = 0;
			m_frameBottomLeft.render(g, x + offsetX, y + offsetY, fScale);
			for (offsetX = m_frameBottomLeft.getBounds().width; offsetX < m_width - m_frameBottomRight.getBounds().width; offsetX += m_frameTop.getBounds().width)
			{
				m_frameBottom.render(g, x + offsetX, y + offsetY, fScale);
			}
			m_frameBottomRight.render(g, x + offsetX, y + offsetY, fScale);
		}

		for (Control control : m_controls)
		{
			if (control.isVisible())
				control.render(g, control.getLocation().x + x, control.getLocation().y + y, fScale);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Control#onMouseEvent(jeva.joystick.InputManager.
	 * InputMouseEvent)
	 */
	@Override
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{
		if (mouseEvent.type == EventType.MouseWheelMoved && m_activeControl != null)
			m_activeControl.onMouseEvent(mouseEvent);
		else
		{
			for (int i = m_controls.size() - 1; i >= 0; i--)
			{
				Control control = m_controls.get(i);

				if (control.isVisible())
				{
					Vector2D relativeLocation = mouseEvent.location.difference(control.getAbsoluteLocation());
					boolean isInBounds = control.getBounds().contains(relativeLocation.x, relativeLocation.y);

					if (isInBounds)
					{
						if (m_lastOver != control)
						{
							if (m_lastOver != null)
								m_lastOver.onLeave();

							m_lastOver = control;
							control.onEnter();
						}

						mouseEvent.isConsumed = true;

						if (mouseEvent.type == EventType.MouseClicked)
						{
							m_activeControl = control;
							m_activeControl.onMouseEvent(mouseEvent);
						} else
							control.onMouseEvent(mouseEvent);

						break;
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.github.jeremywildsmith.jevaengine.graphics.ui.Control#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent event)
	{
		if (m_activeControl != null)
			m_activeControl.onKeyEvent(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Control#update(int)
	 */
	@Override
	public void update(int deltaTime)
	{
		for (Control control : m_controls)
			control.update(deltaTime);
	}

	public void setRenderBackground(boolean renderBackground)
	{
		m_renderBackground = renderBackground;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Control#getBounds()
	 */
	@Override
	public Rectangle getBounds()
	{
		return new Rectangle(0, 0, m_width, m_height);
	}

	public void setWidth(int width)
	{
		m_width = width;
	}

	public void setHeight(int height)
	{
		m_height = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.graphics.ui.Control#onStyleChanged()
	 */
	@Override
	protected void onStyleChanged()
	{
		super.onStyleChanged();

		if (getStyle() != null)
		{
			m_frameFill = getStyle().createFrameFillSprite();
			m_frameLeft = getStyle().createFrameLeftSprite();
			m_frameRight = getStyle().createFrameRightSprite();
			m_frameTop = getStyle().createFrameTopSprite();
			m_frameBottom = getStyle().createFrameBottomSprite();

			m_frameTopLeft = getStyle().createFrameTopLeftSprite();
			m_frameTopRight = getStyle().createFrameTopRightSprite();
			m_frameBottomLeft = getStyle().createFrameBottomLeftSprite();
			m_frameBottomRight = getStyle().createFrameBottomRightSprite();

			m_frameFill.setAnimation("off", AnimationState.Play);
			m_frameLeft.setAnimation("off", AnimationState.Play);
			m_frameRight.setAnimation("off", AnimationState.Play);
			m_frameTop.setAnimation("off", AnimationState.Play);
			m_frameBottom.setAnimation("off", AnimationState.Play);

			m_frameTopLeft.setAnimation("off", AnimationState.Play);
			m_frameTopRight.setAnimation("off", AnimationState.Play);
			m_frameBottomLeft.setAnimation("off", AnimationState.Play);
			m_frameBottomRight.setAnimation("off", AnimationState.Play);

			for (Control ctrl : m_controls)
				ctrl.setStyle(getStyle());
		}
	}

}
