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
package jeva.joystick;

/**
 * The listener interface for receiving IInputDevice events. The class that is
 * interested in processing a IInputDevice event implements this interface, and
 * the object created with that class is registered with a component using the
 * component's <code>addIInputDeviceListener<code> method. When
 * the IInputDevice event occurs, that object's appropriate
 * method is invoked.
 * 
 * @author Jeremy. A. W
 */
public interface IInputDeviceListener
{

	/**
	 * Mouse clicked.
	 * 
	 * @param e
	 *            the e
	 */
	void mouseClicked(InputManager.InputMouseEvent e);

	/**
	 * Mouse moved.
	 * 
	 * @param e
	 *            the e
	 */
	void mouseMoved(InputManager.InputMouseEvent e);

	/**
	 * Mouse button state changed.
	 * 
	 * @param e
	 *            the e
	 */
	void mouseButtonStateChanged(InputManager.InputMouseEvent e);

	/**
	 * Mouse wheel moved.
	 * 
	 * @param e
	 *            the e
	 */
	void mouseWheelMoved(InputManager.InputMouseEvent e);

	/**
	 * Mouse left.
	 * 
	 * @param e
	 *            the e
	 */
	void mouseLeft(InputManager.InputMouseEvent e);

	/**
	 * Mouse entered.
	 * 
	 * @param e
	 *            the e
	 */
	void mouseEntered(InputManager.InputMouseEvent e);

	/**
	 * Key typed.
	 * 
	 * @param e
	 *            the e
	 */
	void keyTyped(InputManager.InputKeyEvent e);

	/**
	 * Key down.
	 * 
	 * @param e
	 *            the e
	 */
	void keyDown(InputManager.InputKeyEvent e);

	/**
	 * Key up.
	 * 
	 * @param e
	 *            the e
	 */
	void keyUp(InputManager.InputKeyEvent e);
}
