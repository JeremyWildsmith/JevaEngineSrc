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
package jevarpg.net.client;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
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
public class Main implements WindowListener, KeyListener
{
	private static final int WINX = 1024;
	private static final int WINY = 768;

	private boolean m_isFullscreen = false;

	private int m_displayX = 0;
	private int m_displayY = 0;

	private Frame m_frame;

	private volatile boolean m_terminate = false;

	public static void main(String[] args)
	{
		Main m = new Main();
		m.entry(args);
	}

	public void entry(String[] args)
	{
		m_frame = new Frame();
		try
		{
			if (args.length >= 1)
			{
				String[] resolution = args[0].split("x");

				if (resolution.length != 2)
					throw new RuntimeException("Invalid resolution");

				try
				{
					m_displayX = Integer.parseInt(resolution[0]);
					m_displayY = Integer.parseInt(resolution[1]);
				} catch (NumberFormatException e)
				{
					throw new RuntimeException("Resolution improperly formed");
				}
			}

			if (m_displayX <= 0 || m_displayY <= 0)
			{
				m_displayX = WINX;
				m_displayY = WINY;
			} else
				m_isFullscreen = true;

			SwingUtilities.invokeAndWait(new Runnable()
			{

				public void run()
				{
					m_frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(""), new Point(), "trans"));
					m_frame.setVisible(true);
					m_frame.addKeyListener(Main.this);

					m_frame.setIgnoreRepaint(true);
					m_frame.setBackground(Color.black);
					m_frame.setResizable(false);
					m_frame.setTitle("JevaEngine - Underground");
					m_frame.setSize(m_displayX, m_displayY);
					m_frame.setVisible(true);
					m_frame.addWindowListener(Main.this);

					GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

					if (m_isFullscreen)
					{
						device.setFullScreenWindow(m_frame);
						device.setDisplayMode(new DisplayMode(m_displayX, m_displayY, 32, 60));
					}
				}
			});
		} catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "An error occured initializing the game: " + ex);
			return;
		}

		Core.initializeCore(new ClientGame(), new StatelessResourceLibrary());

		Game game = Core.getService(Game.class);

		game.init(m_frame, WINX, WINY);

		final int targetTime = 1000 / 60 + 20;

		long lastTime = System.nanoTime() / 1000000;
		long curTime = lastTime;

		while (!m_terminate)
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

		game.dispose();
		m_frame.setVisible(false);
		System.exit(0);
	}

	@Override
	public void windowActivated(WindowEvent arg0)
	{
	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		m_terminate = true;
	}

	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{
	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
			m_frame.dispatchEvent(new WindowEvent(m_frame, WindowEvent.WINDOW_CLOSING));
	}

	@Override
	public void keyTyped(KeyEvent e)
	{
		// TODO Auto-generated method stub

	}
}
