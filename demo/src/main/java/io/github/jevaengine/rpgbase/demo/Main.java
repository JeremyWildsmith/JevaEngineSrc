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
package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.IAssetStreamFactory;
import io.github.jevaengine.game.FrameRenderer;
import io.github.jevaengine.game.FrameRenderer.RenderFitMode;
import io.github.jevaengine.game.GameDriver;
import io.github.jevaengine.game.IGameFactory;
import io.github.jevaengine.game.IRenderer;
import io.github.jevaengine.joystick.FrameInputSource;
import io.github.jevaengine.joystick.IInputSource;
import io.github.jevaengine.rpgbase.RpgEntityFactory;
import io.github.jevaengine.rpgbase.dialogue.IDialogueRouteFactory;
import io.github.jevaengine.rpgbase.dialogue.ScriptedDialogueRouteFactory;
import io.github.jevaengine.world.entity.IEntityFactory;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main implements WindowListener
{
	private static final int WINX = 1280;
	private static final int WINY = 720;

	private int m_displayX = 0;
	private int m_displayY = 0;

	private JFrame m_frame;
	private GameDriver m_gameDriver;
	
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
			}
			
			SwingUtilities.invokeAndWait(new Runnable()
			{

				public void run()
				{
					m_frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(""), new Point(), "trans"));
					m_frame.setVisible(true);

					m_frame.setIgnoreRepaint(true);
					m_frame.setBackground(Color.black);
					m_frame.setResizable(false);
					m_frame.setTitle("JevaEngine - Underground");
					m_frame.setSize(m_displayX, m_displayY);
					m_frame.setVisible(true);
					m_frame.addWindowListener(Main.this);
				}
			});
		} catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "An error occured initializing the game: " + ex);
			return;
		}

		Injector injector = Guice.createInjector(new EngineModule());
		m_gameDriver = injector.getInstance(GameDriver.class);
		m_gameDriver.begin();
		//m_frame.dispose();
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		m_gameDriver.stop();
	}
	
	private final class EngineModule extends AbstractModule
	{
		@Override
		protected void configure()
		{
			bind(IInputSource.class).toInstance(FrameInputSource.create(m_frame));
			bind(IRenderer.class).toInstance(new FrameRenderer(m_frame, true, RenderFitMode.Stretch));
			bind(IGameFactory.class).to(DemoGameFactory.class);
			bind(IEntityFactory.class).to(RpgEntityFactory.class).asEagerSingleton();
			bind(IAssetStreamFactory.class).to(DemoAssetStreamFactory.class).asEagerSingleton();
			bind(IDialogueRouteFactory.class).to(ScriptedDialogueRouteFactory.class);
		}
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) { }
	
	@Override
	public void windowClosed(WindowEvent arg0) { }

	@Override
	public void windowDeactivated(WindowEvent arg0) { }

	@Override
	public void windowDeiconified(WindowEvent arg0) { }

	@Override
	public void windowIconified(WindowEvent arg0) { }

	@Override
	public void windowOpened(WindowEvent arg0) { }
}
