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
package io.github.jevaengine.client;

import io.github.jevaengine.Core;
import io.github.jevaengine.game.DefaultGame;
import io.github.jevaengine.rpgbase.RpgLibrary;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main implements WindowListener, KeyListener
{
	private static final int WINX = 800;
	private static final int WINY = 600;

	private boolean m_isFullscreen = false;

	private int m_displayX = 0;
	private int m_displayY = 0;

	private JFrame m_frame;

	private volatile boolean m_runGame = true;
	
	public static void main(String[] args)
	{
		Main m = new Main();
		m.entry(args);
	}

	public void entry(String[] args)
	{
		m_frame = new JFrame();
		
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
					m_frame.setTitle("JevaEngine - Client");
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

		Core.initialize(new ClientGame(), new RpgLibrary(), Executors.newFixedThreadPool(8));

		DefaultGame game = Core.getService(DefaultGame.class);

		game.init(m_frame);
		
		long lastTime;
		
		do
		{
			lastTime = System.currentTimeMillis();
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
				Thread.interrupted();
			}
			game.render();
		} while(m_runGame && game.update((int)(System.currentTimeMillis() - lastTime)));

		m_frame.dispose();
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
		m_runGame = false;
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
