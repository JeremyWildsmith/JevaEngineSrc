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
package io.github.jevaengine.joystick;

import io.github.jevaengine.joystick.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.SwingUtilities;

public class FrameInputSource implements IInputSource, MouseMotionListener, MouseListener, KeyListener, MouseWheelListener
{
	private final ConcurrentLinkedQueue<IInputEvent> m_events = new ConcurrentLinkedQueue<IInputEvent>();

	private boolean m_isDragging = false;

	public static FrameInputSource create(final Component target)
	{
		final FrameInputSource manager = new FrameInputSource();
		
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{
				@Override
				public void run()
				{
					target.addKeyListener(manager);
					target.addMouseMotionListener(manager);
					target.addMouseListener(manager);
					target.addMouseWheelListener(manager);
				}
			});
		} catch (InvocationTargetException | InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		return manager;
	}

	@Override
	public void process(IInputSourceProcessor processor)
	{
		while (!m_events.isEmpty())
		{
			m_events.remove().relay(processor);
		}
	}

	public void mouseMoved(MouseEvent e)
	{
		m_isDragging = false;
		m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseMoved, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
	}

	public void mouseClicked(MouseEvent e)
	{
		m_isDragging = false;
		m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseClicked, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
	}

	public void keyTyped(KeyEvent e)
	{
		m_events.add(new InputKeyEvent(InputKeyEvent.KeyEventType.KeyTyped, e.getKeyCode(), e.getKeyChar()));
	}

	public void mouseDragged(MouseEvent e)
	{
		if (SwingUtilities.isLeftMouseButton(e))
		{
			if (!m_isDragging)
				m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseClicked, new Vector2D(e.getX(), e.getY()), MouseButton.Left, false, false));

			m_isDragging = true;
			m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseMoved, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, true));
		} else
		{
			if (!m_isDragging)
				m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseClicked, new Vector2D(e.getX(), e.getY()), MouseButton.Right, false, false));

			m_isDragging = true;
			m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseMoved, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
		}
	}

	public void mousePressed(MouseEvent e)
	{
		m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MousePressed, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), true, false));
	}

	public void mouseReleased(MouseEvent e)
	{
		m_isDragging = false;
		m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseReleased, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
	}

	public void mouseEntered(MouseEvent e)
	{
		m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseEntered, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
	}

	public void mouseExited(MouseEvent e)
	{
		m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseLeft, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
	}

	public void keyPressed(KeyEvent e)
	{
		m_events.add(new InputKeyEvent(InputKeyEvent.KeyEventType.KeyDown, e.getKeyCode(), e.getKeyChar()));
	}

	public void keyReleased(KeyEvent e)
	{
		m_events.add(new InputKeyEvent(InputKeyEvent.KeyEventType.KeyUp, e.getKeyCode(), e.getKeyChar()));
	}

	public void mouseWheelMoved(MouseWheelEvent e)
	{
		m_events.add(new InputMouseEvent(InputMouseEvent.MouseEventType.MouseWheelMoved, new Vector2D(e.getX(), e.getY()), MouseButton.Left, false, false, e.getUnitsToScroll()));
	}
}
