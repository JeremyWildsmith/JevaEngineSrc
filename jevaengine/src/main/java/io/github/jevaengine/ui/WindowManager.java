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
package io.github.jevaengine.ui;

import java.awt.Graphics2D;

import io.github.jevaengine.joystick.InputManager;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.EventType;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.util.StaticSet;

public class WindowManager
{

	StaticSet<Window> m_windows;

	public WindowManager()
	{
		m_windows = new StaticSet<Window>();
	}

	protected Window[] getWindows()
	{
		return m_windows.toArray(new Window[m_windows.size()]);
	}
	
	public final void addWindow(Window window)
	{
		if (m_windows.contains(window))
			throw new DuplicateWindowException();

		window.setManager(this);
		
		m_windows.add(window);
	}

	public final void removeWindow(Window window)
	{
		if (!m_windows.contains(window))
			throw new NoSuchWindowException();

		window.setManager(null);
		m_windows.remove(window);
	}

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

				boolean isCursorOverTop = topWindow.isVisible() && topWindow.getBounds().contains(topRelativePoint);

				if (!mouseEvent.isConsumed && window.getBounds().contains(relativePoint))
				{
					if (window.isFocusable() && (!isCursorOverTop && (mouseEvent.isDragging || mouseEvent.type == EventType.MouseClicked)))
						moveToTop = topWindow = window;

					if (mouseEvent.isDragging && window.isMovable() && window == topWindow)
					{
						window.setLocation(window.getLocation().add(mouseEvent.delta));
					} else
					{
						if (window == topWindow || !isCursorOverTop)
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

	public boolean onKeyEvent(InputManager.InputKeyEvent keyEvent)
	{
		for (int i = 0; i < m_windows.size(); i++)
		{
			Window windowBuffer = m_windows.get(i);
			windowBuffer.onKeyEvent(keyEvent);
		}

		return true;
	}

	public void render(Graphics2D g, int x, int y, float fScale)
	{

		for (int i = m_windows.size() - 1; i >= 0; i--)
		{
			if (m_windows.get(i).isVisible())
				m_windows.get(i).render(g, x + m_windows.get(i).getLocation().x, y + m_windows.get(i).getLocation().y, fScale);
		}
	}

	public void update(int deltaTime)
	{
		for (Window w : m_windows)
			w.update(deltaTime);
	}
}
