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

import jeva.joystick.InputManager.InputKeyEvent;
import jeva.math.Vector2D;

/**
 * The Class MenuStrip.
 */
public class MenuStrip extends Panel
{

	/**
	 * Instantiates a new menu strip.
	 */
	public MenuStrip()
	{
		super(200, 0);
		this.setVisible(false);
	}

	/**
	 * Sets the context.
	 * 
	 * @param commands
	 *            the commands
	 * @param listener
	 *            the listener
	 */
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

	/**
	 * The listener interface for receiving IMenuStrip events. The class that is
	 * interested in processing a IMenuStrip event implements this interface,
	 * and the object created with that class is registered with a component
	 * using the component's <code>addIMenuStripListener<code> method. When
	 * the IMenuStrip event occurs, that object's appropriate
	 * method is invoked.
	 * 
	 * @see IMenuStripEvent
	 */
	public interface IMenuStripListener
	{

		/**
		 * On command.
		 * 
		 * @param bommand
		 *            the bommand
		 */
		void onCommand(String bommand);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.graphics.ui.Panel#onKeyEvent(jeva.joystick.InputManager.InputKeyEvent
	 * )
	 */
	@Override
	public void onKeyEvent(InputKeyEvent keyEvent)
	{
	}
}
