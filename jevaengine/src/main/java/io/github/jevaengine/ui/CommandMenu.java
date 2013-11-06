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

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import io.github.jevaengine.Script;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent.EventType;
import io.github.jevaengine.math.Vector2D;

public final class CommandMenu extends Window
{

	private TextArea m_commandOutArea;

	private TextArea m_commandIn;

	private Script m_script = new Script(new CommandMenuScriptContext());

	private boolean m_isCtrlDown = false;

	public CommandMenu(UIStyle style, int width, int height)
	{
		super(style, Math.max(100, width), Math.max(300, height));
		this.setVisible(false);

		Rectangle bounds = this.getBounds();

		m_commandOutArea = new TextArea(Color.green, (int) (bounds.width - 20), (int) (bounds.height - 210));
		m_commandOutArea.setLocation(new Vector2D(10, 10));
		m_commandOutArea.setText("Core Command Interface.\n");
		m_commandOutArea.setRenderBackground(false);
		this.addControl(m_commandOutArea);

		m_commandIn = new TextArea(Color.white, (int) (bounds.width - 40), 180);
		m_commandIn.setEditable(true);
		m_commandIn.setLocation(new Vector2D(10, bounds.height - 200));
		m_commandIn.setRenderBackground(false);
		this.addControl(m_commandIn);

		this.addControl(new Label(">", Color.red), new Vector2D(3, bounds.height - 200));

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.github.jeremywildsmith.jevaengine.graphics.ui.Panel#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent e)
	{
		if (e.keyCode == KeyEvent.VK_CONTROL && e.type != EventType.KeyTyped)
		{
			m_isCtrlDown = true;
		} else if (m_isCtrlDown && e.type == EventType.KeyTyped)
		{
			m_isCtrlDown = false;
			if (Character.toLowerCase(e.keyChar) == 'e')
			{
				try
				{
					Object out = m_script.evaluate(m_commandIn.getText());

					if (out != null)
						m_commandOutArea.appendText("Evaluation: " + out.toString() + "\n");

					m_commandIn.setText("");
				} catch (Exception ex)
				{
					m_commandOutArea.appendText("Error occured while executing script: " + ex.toString() + "\n");
					m_commandOutArea.scrollToEnd();
				}
			}
		} else
			super.onKeyEvent(e);
	}

	public class CommandMenuScriptContext
	{

		public void echo(String s)
		{
			m_commandOutArea.appendText(s + "\n");
		}

		public void clear()
		{
			m_commandOutArea.setText("");
		}
	}
}
