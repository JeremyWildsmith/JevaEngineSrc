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

import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.math.Vector2D;

public class MenuStrip extends Panel
{

	public MenuStrip()
	{
		super(200, 0);
		this.setVisible(false);
	}

	public void setContext(final String[] commands, final IMenuStripListener listener)
	{
		this.clearControls();

		int lastY = 0;
		int largestX = 0;

		for (int i = 0; i < commands.length; i++)
		{
			final String bommand = commands[i];

			Button cmd = new Button(commands[i])
			{
				private final String m_command = bommand;

				@Override
				public void onButtonPress()
				{
					listener.onCommand(m_command);

					MenuStrip.this.setVisible(false);
				}

			};

			cmd.setLocation(new Vector2D(0, lastY));

			this.addControl(cmd);

			lastY += cmd.getBounds().height + 8;

			largestX = Math.max(largestX, cmd.getBounds().width);
		}

		this.setHeight(lastY + 5);
		this.setWidth(largestX + 15);
		this.setVisible(true);
	}

	public interface IMenuStripListener
	{

		void onCommand(String bommand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.github.jeremywildsmith.jevaengine.graphics.ui.Panel#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent keyEvent)
	{
	}
}
