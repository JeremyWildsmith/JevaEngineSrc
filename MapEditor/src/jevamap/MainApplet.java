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
package jevamap;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;

import jeva.Core;
import jeva.game.Game;

public class MainApplet extends JApplet implements Runnable
{
	private static final long serialVersionUID = 1L;

	@Override
	public void init()
	{
		this.setIgnoreRepaint(true);

		// Game's init function takes a Component as the first argument
		// It tries to add itself (it is a panel) to the JApplet Component
		// Core.getService(Game.class).init(this, this);

		Thread tBuffer = new Thread(this);
		tBuffer.start();
	}

	public void run()
	{
		long lastTime = System.currentTimeMillis();
		long curTime;

		while (true)
		{
			curTime = System.currentTimeMillis();

			Core.getService(Game.class).update((int) (curTime - lastTime));

			lastTime = curTime;
			try
			{
				Thread.sleep(20);
			} catch (InterruptedException ex)
			{
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
