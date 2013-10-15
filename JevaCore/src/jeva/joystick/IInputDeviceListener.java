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


public interface IInputDeviceListener
{

	
	void mouseClicked(InputManager.InputMouseEvent e);

	
	void mouseMoved(InputManager.InputMouseEvent e);

	
	void mouseButtonStateChanged(InputManager.InputMouseEvent e);

	
	void mouseWheelMoved(InputManager.InputMouseEvent e);

	
	void mouseLeft(InputManager.InputMouseEvent e);

	
	void mouseEntered(InputManager.InputMouseEvent e);

	
	void keyTyped(InputManager.InputKeyEvent e);

	
	void keyDown(InputManager.InputKeyEvent e);

	
	void keyUp(InputManager.InputKeyEvent e);
}
