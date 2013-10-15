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

import jeva.joystick.InputManager;
import jeva.joystick.InputManager.InputMouseEvent.EventType;
import jeva.math.Vector2D;
import jeva.util.StaticSet;

public class BasicWindowManager implements IWindowManager
{

	StaticSet<Window> m_windows;


	public BasicWindowManager()
	{
		m_windows = new StaticSet<Window>();
	}

	@Override
	public void addWindow(Window window)
	{
		if (m_windows.contains(window))
			throw new DuplicateWindowException();

		m_windows.add(window);
	}

	@Override
	public void removeWindow(Window window)
	{
		if (!m_windows.contains(window))
			throw new NoSuchWindowException();

		m_windows.remove(window);
	}

	@Override
	public void onMouseEvent(InputManager.InputMouseEvent mouseEvent)
	{
		Window moveToTop = null;
		Window topWindow = m_windows.size() > 0 ? m_windows.get(0) : null;

		for (Window window : m_windows)
		{
			if (window.isVisible())
			{
				Vector2D relativePoint = mouseEvent.location.difference(window.getLocation());
				Vector2D topRelativePoint = mouseEvent.location.difference(topWindow.getLocation());

				boolean isCursorOverTop = topWindow.isVisible() && topWindow.getBounds().contains(topRelativePoint.x, topRelativePoint.y);

				if (window.getBounds().contains(relativePoint.x, relativePoint.y))
				{
					if (window.isFocusable() && (!isCursorOverTop && mouseEvent.isDragging || mouseEvent.type == EventType.MouseClicked))
						moveToTop = topWindow = window;

					if (mouseEvent.isDragging && window.isMovable() && window == topWindow)
					{
						window.setLocation(window.getLocation().add(mouseEvent.delta));
					} else
					{
						if (window == topWindow)
							window.onMouseEvent(mouseEvent);
						else if (mouseEvent.type == EventType.MouseMoved && !isCursorOverTop)
							window.onMouseEvent(mouseEvent);
					}
				}
			}
		}

		if (moveToTop != null && m_windows.contains(moveToTop))
		{
			m_windows.remove(moveToTop);
			m_windows.add(0, moveToTop);
		}
	}

	@Override
	public boolean onKeyEvent(InputManager.InputKeyEvent keyEvent)
	{
		for (int i = 0; i < m_windows.size(); i++)
		{
			Window windowBuffer = m_windows.get(i);
			windowBuffer.onKeyEvent(keyEvent);
		}

		return true;
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

		for (int i = m_windows.size() - 1; i >= 0; i--)
		{
			if (m_windows.get(i).isVisible())
				m_windows.get(i).render(g, x + m_windows.get(i).getLocation().x, y + m_windows.get(i).getLocation().y, fScale);
		}
	}

	@Override
	public void update(int deltaTime)
	{
		for (Window w : m_windows)
			w.update(deltaTime);
	}
}
