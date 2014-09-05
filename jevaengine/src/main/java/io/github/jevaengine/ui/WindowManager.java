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

import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.joystick.InputMouseEvent.MouseEventType;
import io.github.jevaengine.math.Vector2D;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public final class WindowManager
{
	private final ArrayList<Window> m_windows = new ArrayList<>();
	private final Queue<Window> m_windowProcessQueue = new LinkedList<>();
	
	private final Vector2D m_resolution;
	
	public WindowManager(Vector2D resolution)
	{
		m_resolution = resolution;
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
		m_windowProcessQueue.add(window);
		setFocusedWindow(window);
	}

	public final void removeWindow(Window window)
	{
		if (!m_windows.contains(window))
			throw new NoSuchWindowException();

		window.setManager(null);
		m_windows.remove(window);
		m_windowProcessQueue.remove(window);
	}

	public void setFocusedWindow(Window window)
	{
		if(m_windows.contains(window))
		{
			if(m_windows.get(0) != window)
			{
				m_windows.get(0).clearFocus();
				m_windows.remove(window);
				m_windows.add(0, window);
				window.setFocus();
			}
		}
	}
	
	public void centerWindow(Window window)
	{
		if(!m_windows.contains(window))
			return;
		
		window.setLocation(new Vector2D((m_resolution.x - window.getBounds().width) / 2,
						 (m_resolution.y - window.getBounds().height) / 2));
	}
	
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{
		Window moveToTop = null;
		Window topWindow = m_windows.size() > 0 ? m_windows.get(0) : null;

		m_windowProcessQueue.clear();
		m_windowProcessQueue.addAll(m_windows);
		
		for (Window window; (window = m_windowProcessQueue.poll()) != null;)
		{
			if (window.isVisible())
			{
				Vector2D relativePoint = mouseEvent.location.difference(window.getLocation());
				Vector2D topRelativePoint = mouseEvent.location.difference(topWindow.getLocation());

				boolean isCursorOverTop = topWindow.isVisible() && topWindow.getBounds().contains(topRelativePoint);

				if (window.getBounds().contains(relativePoint))
				{
					if (window.isFocusable() && 
						(!isCursorOverTop && (mouseEvent.type == MouseEventType.MousePressed)))
						moveToTop = topWindow = window;

					if (mouseEvent.isDragging && window.isMovable() && window == topWindow)
					{
						if(!window.onMouseEvent(mouseEvent))
							window.setLocation(window.getLocation().add(mouseEvent.delta));
					} else
					{
						if (window == topWindow || !isCursorOverTop)
							window.onMouseEvent(mouseEvent);
						else if (mouseEvent.type == MouseEventType.MouseMoved && !isCursorOverTop)
							window.onMouseEvent(mouseEvent);
					}
				}
			}
		}

		if (moveToTop != null && m_windows.contains(moveToTop))
			setFocusedWindow(moveToTop);
	}

	public boolean onKeyEvent(InputKeyEvent keyEvent)
	{
		if(!m_windows.isEmpty())
			m_windows.get(0).onKeyEvent(keyEvent);
		
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
		m_windowProcessQueue.clear();
		m_windowProcessQueue.addAll(m_windows);
		for (Window window; (window = m_windowProcessQueue.poll()) != null;)
			window.update(deltaTime);
	}
}
