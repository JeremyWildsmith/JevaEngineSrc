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
import java.util.ArrayList;

import com.sun.istack.internal.Nullable;

import jeva.graphics.AnimationState;
import jeva.graphics.Sprite;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.joystick.InputManager.InputMouseEvent.EventType;
import jeva.math.Vector2D;

/**
 * The Class Panel.
 */
public abstract class Panel extends Control
{

	/** The m_render background. */
	private boolean m_renderBackground;

	/** The m_frame fill. */
	private Sprite m_frameFill;

	/** The m_frame left. */
	private Sprite m_frameLeft;

	/** The m_frame right. */
	private Sprite m_frameRight;

	/** The m_frame top. */
	private Sprite m_frameTop;

	/** The m_frame bottom. */
	private Sprite m_frameBottom;

	/** The m_frame top left. */
	private Sprite m_frameTopLeft;

	/** The m_frame top right. */
	private Sprite m_frameTopRight;

	/** The m_frame bottom left. */
	private Sprite m_frameBottomLeft;

	/** The m_frame bottom right. */
	private Sprite m_frameBottomRight;

	/** The m_width. */
	private int m_width;

	/** The m_height. */
	private int m_height;

	/** The m_active control. */
	private Control m_activeControl;

	/** The m_last over. */
	private Control m_lastOver;

	/** The m_controls. */
	private ArrayList<Control> m_controls;

	/** The m_garbage controls. */
	private ArrayList<Control> m_garbageControls;

	/** The m_adding controls. */
	private ArrayList<Control> m_addingControls;

	/**
	 * Instantiates a new panel.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @param renderBackground
	 *            the render background
	 */
	public Panel(int width, int height, boolean renderBackground)
	{
		m_width = width;
		m_height = height;
		m_renderBackground = renderBackground;

		m_controls = new ArrayList<Control>();
		m_garbageControls = new ArrayList<Control>();
		m_addingControls = new ArrayList<Control>();
	}

	/**
	 * Instantiates a new panel.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public Panel(int width, int height)
	{
		this(width, height, true);
	}

	/**
	 * Adds the control.
	 * 
	 * @param control
	 *            the control
	 */
	public void addControl(Control control)
	{
		addControl(control, null);
	}

	/**
	 * Adds the control.
	 * 
	 * @param control
	 *            the control
	 * @param location
	 *            the location
	 */
	public void addControl(Control control, @Nullable Vector2D location)
	{
		if (!m_controls.contains(control))
		{
			m_addingControls.add(control);
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

	/**
	 * Removes the control.
	 * 
	 * @param control
	 *            the control
	 */
	public void removeControl(Control control)
	{
		if (m_controls.contains(control))
		{
			control.setParent(null);
			m_garbageControls.add(control);
		}
	}

	/**
	 * Clear controls.
	 */
	public void clearControls()
	{
		m_activeControl = null;

		for (Control ctrl : m_controls)
			removeControl(ctrl);
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
	 * @see jeva.graphics.ui.Control#onMouseEvent(jeva.joystick.InputManager.
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
	 * jeva.graphics.ui.Control#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
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
	 * @see jeva.graphics.ui.Control#update(int)
	 */
	@Override
	public void update(int deltaTime)
	{
		for (Control control : m_addingControls)
			m_controls.add(control);

		for (Control control : m_garbageControls)
			m_controls.remove(control);

		m_addingControls.clear();
		m_garbageControls.clear();

		for (Control control : m_controls)
			control.update(deltaTime);
	}

	/**
	 * Sets the render background.
	 * 
	 * @param renderBackground
	 *            the new render background
	 */
	public void setRenderBackground(boolean renderBackground)
	{
		m_renderBackground = renderBackground;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#getBounds()
	 */
	@Override
	public Rectangle getBounds()
	{
		return new Rectangle(0, 0, m_width, m_height);
	}

	/**
	 * Sets the width.
	 * 
	 * @param width
	 *            the new width
	 */
	public void setWidth(int width)
	{
		m_width = width;
	}

	/**
	 * Sets the height.
	 * 
	 * @param height
	 *            the new height
	 */
	public void setHeight(int height)
	{
		m_height = height;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.graphics.ui.Control#onStyleChanged()
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
