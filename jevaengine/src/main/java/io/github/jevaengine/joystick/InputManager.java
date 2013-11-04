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

import java.awt.Component;
import java.awt.event.*;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;

import javax.swing.SwingUtilities;

import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;

public class InputManager implements MouseMotionListener, MouseListener, KeyListener, MouseWheelListener
{

	private final LinkedList<IInputEvent> m_events = new LinkedList<IInputEvent>();

	private boolean m_isDragging = false;

	protected InputManager()
	{
	}

	public static InputManager create(final Component target)
	{
		final InputManager manager = new InputManager();
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

	public void process(IInputDeviceListener callback)
	{
		synchronized (m_events)
		{
			while (!m_events.isEmpty())
			{
				m_events.remove().relay(callback);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
	 */
	public void mouseMoved(MouseEvent e)
	{
		synchronized (m_events)
		{
			m_isDragging = false;
			m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseMoved, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
	 */
	public void mouseClicked(MouseEvent e)
	{
		synchronized (m_events)
		{
			m_isDragging = false;
			m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseClicked, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped(KeyEvent e)
	{
		synchronized (m_events)
		{
			m_events.add(new InputKeyEvent(InputKeyEvent.EventType.KeyTyped, e.getKeyCode(), e.getKeyChar()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent
	 * )
	 */
	public void mouseDragged(MouseEvent e)
	{
		synchronized (m_events)
		{

			if (SwingUtilities.isLeftMouseButton(e))
			{
				if (!m_isDragging)
					m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseClicked, new Vector2D(e.getX(), e.getY()), MouseButton.Left, false, false));

				m_isDragging = true;
				m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseMoved, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, true));
			} else
			{
				if (!m_isDragging)
					m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseClicked, new Vector2D(e.getX(), e.getY()), MouseButton.Right, false, false));

				m_isDragging = true;
				m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseMoved, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
	 */
	public void mousePressed(MouseEvent e)
	{
		synchronized (m_events)
		{
			m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MousePressed, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), true, false));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
	 */
	public void mouseReleased(MouseEvent e)
	{
		synchronized (m_events)
		{
			m_isDragging = false;
			m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseReleased, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
	 */
	public void mouseEntered(MouseEvent e)
	{
		m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseEntered, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
	 */
	public void mouseExited(MouseEvent e)
	{
		m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseLeft, new Vector2D(e.getX(), e.getY()), MouseButton.fromButton(e.getButton()), false, false));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e)
	{
		synchronized (m_events)
		{
			m_events.add(new InputKeyEvent(InputKeyEvent.EventType.KeyDown, e.getKeyCode(), e.getKeyChar()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e)
	{
		synchronized (m_events)
		{
			m_events.add(new InputKeyEvent(InputKeyEvent.EventType.KeyUp, e.getKeyCode(), e.getKeyChar()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.
	 * MouseWheelEvent)
	 */
	public void mouseWheelMoved(MouseWheelEvent e)
	{
		synchronized (m_events)
		{
			m_events.add(new InputMouseEvent(InputMouseEvent.EventType.MouseWheelMoved, new Vector2D(e.getX(), e.getY()), MouseButton.Left, false, false, e.getUnitsToScroll()));
		}
	}

	private interface IInputEvent
	{

		void relay(IInputDeviceListener handler);
	}

	public static class InputKeyEvent implements IInputEvent
	{

		public enum EventType
		{

			KeyTyped,

			KeyDown,

			KeyUp,
		}

		public EventType type;

		public int keyCode;

		public char keyChar;

		public boolean isConsumed;

		protected InputKeyEvent(EventType _type, int _keyCode, char _keyChar)
		{
			isConsumed = false;
			type = _type;
			keyCode = _keyCode;
			keyChar = _keyChar;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.joystick.InputManager.IInputEvent#relay(jeva.joystick.
		 * IInputDeviceListener)
		 */
		public void relay(IInputDeviceListener handler)
		{
			switch (type)
			{
				case KeyTyped:
					handler.keyTyped(this);
					break;
				case KeyUp:
					handler.keyUp(this);
					break;
				case KeyDown:
					handler.keyDown(this);
					break;
			}
		}
	}

	public static class InputMouseEvent implements IInputEvent
	{

		public enum EventType
		{

			MousePressed,

			MouseReleased,

			MouseMoved,

			MouseClicked,

			MouseWheelMoved,

			MouseLeft,

			MouseEntered,
		}

		public enum MouseButton
		{

			Unknown,

			Left,

			Middle,

			Right;

			public static MouseButton fromButton(int button)
			{
				switch (button)
				{
					case MouseEvent.BUTTON1:
						return Left;
					case MouseEvent.BUTTON2:
						return Middle;
					case MouseEvent.BUTTON3:
						return Right;
					default:
						return Unknown;
				}
			}
		}

		private static Vector2D lastLocation = new Vector2D();

		public EventType type;

		public Vector2D location;

		public Vector2D delta;

		public MouseButton mouseButton;

		public boolean mouseButtonState;

		public int deltaMouseWheel;

		public boolean isConsumed;

		public boolean isDragging;

		public InputMouseEvent(InputMouseEvent event)
		{
			type = event.type;
			location = new Vector2D(event.location);
			mouseButton = event.mouseButton;
			mouseButtonState = event.mouseButtonState;
			deltaMouseWheel = 0;
			isConsumed = event.isConsumed;
			isDragging = event.isDragging;

			delta = new Vector2D(event.location.difference(lastLocation));
		}

		protected InputMouseEvent(EventType _type, Vector2D _location, MouseButton _mouseButton, boolean _mouseButtonState, boolean _isDragging)
		{
			type = _type;
			location = _location;
			mouseButton = _mouseButton;
			mouseButtonState = _mouseButtonState;
			deltaMouseWheel = 0;
			isConsumed = false;
			isDragging = _isDragging;

			delta = _location.difference(lastLocation);

			lastLocation = _location;
		}

		protected InputMouseEvent(EventType _type, Vector2D _location, MouseButton _mouseButton, boolean _mouseButtonState, boolean _isDragging, int _iDeltaMouseWheel)
		{
			type = _type;
			location = _location;
			mouseButton = _mouseButton;
			mouseButtonState = _mouseButtonState;
			deltaMouseWheel = _iDeltaMouseWheel;
			lastLocation = _location;
			isDragging = _isDragging;

			delta = _location.difference(lastLocation);

			lastLocation = _location;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.joystick.InputManager.IInputEvent#relay(jeva.joystick.
		 * IInputDeviceListener)
		 */
		public void relay(IInputDeviceListener handler)
		{
			switch (type)
			{
				case MousePressed:
				case MouseReleased:
					handler.mouseButtonStateChanged(this);
					break;
				case MouseMoved:
					handler.mouseMoved(this);
					break;
				case MouseClicked:
					handler.mouseClicked(this);
					break;
				case MouseWheelMoved:
					handler.mouseWheelMoved(this);
				case MouseLeft:
					handler.mouseLeft(this);
					break;
				case MouseEntered:
					handler.mouseEntered(this);
					break;
			}
		}
	}
}
