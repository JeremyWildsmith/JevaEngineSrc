/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jevarpg.net.server;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import proguard.annotation.KeepApplication;
import jeva.Core;
import jeva.game.Game;
import jevarpg.library.StatelessResourceLibrary;

/**
 * 
 * @author Jeremy. A. W
 */

@KeepApplication
public class Main
{
	private static final int WINX = 800;
	private static final int WINY = 600;

	public static void main(String[] args)
	{
		final Frame frameBuffer = new Frame();

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

		Core.initializeCore(new ServerGame(), new StatelessResourceLibrary());

		Game game = Core.getService(Game.class);

		game.init(frameBuffer, WINX, WINY);

		final int targetTime = 1000 / 60 + 20;

		long lastTime = System.nanoTime() / 1000000;
		long curTime = lastTime;

		while (true)
		{
			curTime = System.nanoTime() / 1000000;

			game.update((int) ((curTime - lastTime)));
			game.render();

			int cycleLength = (int) (curTime - lastTime);

			lastTime = curTime;

			try
			{
				if (targetTime > cycleLength)
					Thread.sleep(targetTime - cycleLength);
			} catch (InterruptedException ex)
			{
				Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
}
