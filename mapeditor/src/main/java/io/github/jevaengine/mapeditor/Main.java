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
package io.github.jevaengine.mapeditor;

import io.github.jevaengine.Core;
import io.github.jevaengine.game.Game;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main
{
	private static final int WINX = 1024;
	private static final int WINY = 768;

	private static void lookAndFeel()
	{
		try
		{
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex)
		{
			java.util.logging.Logger.getLogger(NewMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex)
		{
			java.util.logging.Logger.getLogger(NewMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex)
		{
			java.util.logging.Logger.getLogger(NewMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(NewMap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
	}
	
	public static void main(String[] args) throws InterruptedException
	{
		lookAndFeel();
		
		Configuration config = new Configuration(null, true);
		
		config.setVisible(true);
		
		if(!config.isLastQueryValid())
		{
			config.dispose();
			return;
		}
		
		final JFrame frameBuffer = new JFrame();

		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{

				public void run()
				{
					frameBuffer.setVisible(true);
					frameBuffer.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(""), new Point(), "trans"));
					frameBuffer.setVisible(true);

					frameBuffer.setIgnoreRepaint(true);
					frameBuffer.setBackground(Color.black);
					frameBuffer.setResizable(false);
					frameBuffer.setTitle("JevaEngine - Underground");
					frameBuffer.setSize(WINX, WINY);
				}
			});
		} catch (InterruptedException | InvocationTargetException ex)
		{
			throw new RuntimeException(ex);
		}
		
		Core.initialize(new MapEditor(config.getBaseDirectory()),
						new MapEditorLibrary(config.getBaseDirectory()));

		
		Game game = Core.getService(Game.class);

		game.init(frameBuffer, WINX, WINY);

		final int targetTime = 1000 / 60 + 20;

		long lastTime = System.nanoTime() / 1000000;
		long curTime;

		while (true)
		{
			curTime = System.nanoTime() / 1000000;

			game.update((int) ((curTime - lastTime)));
			game.render();

			int cycleLength = (int) (curTime - lastTime);

			lastTime = curTime;

			try
			{
				Thread.sleep(Math.max(targetTime - cycleLength, 20));
			} catch (InterruptedException ex)
			{
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
