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
package io.github.jevaengine.worldbuilder;

import io.github.jevaengine.IAssetStreamFactory;
import io.github.jevaengine.game.FrameRenderer;
import io.github.jevaengine.game.FrameRenderer.RenderFitMode;
import io.github.jevaengine.game.GameDriver;
import io.github.jevaengine.game.IGameFactory;
import io.github.jevaengine.game.IRenderer;
import io.github.jevaengine.joystick.FrameInputSource;
import io.github.jevaengine.joystick.IInputSource;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.NullScriptFactory;
import io.github.jevaengine.world.IWorldFactory;
import io.github.jevaengine.world.entity.IEntityFactory;
import io.github.jevaengine.world.entity.NullEntityFactory;
import io.github.jevaengine.world.physics.IPhysicsWorldFactory;
import io.github.jevaengine.world.physics.NullPhysicsWorldFactory;
import io.github.jevaengine.worldbuilder.world.EditorWorldFactory;

import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;

public class Main implements WindowListener
{
	private static final int WINX = 800;
	private static final int WINY = 600;

	private JFrame m_frame;
	private GameDriver m_gameDriver;
	
	private final Logger m_logger = LoggerFactory.getLogger(Main.class);
	
	public static void main(String[] args)
	{
		Main m = new Main();
		m.entry(args);
	}

	public void entry(String[] args)
	{
		String assetSource = new File("").getAbsoluteFile().toString();
		
		if(args.length >= 1)
			assetSource = args[0];
		else
			m_logger.warn("You have not specified a working directiory [ the root folder of your game's assets. ] The active working directory of the builder will be used instead. This is likely not the behavior you want.");
		
		m_frame = new JFrame();
		
		try
		{
			SwingUtilities.invokeAndWait(new Runnable()
			{

				public void run()
				{
					m_frame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage(""), new Point(), "trans"));
					m_frame.setVisible(true);

					m_frame.setIgnoreRepaint(true);
					m_frame.setBackground(Color.black);
					m_frame.setResizable(false);
					m_frame.setTitle("JevaEngine - World Builder");
					m_frame.setSize(WINX, WINY);
					m_frame.setVisible(true);
					m_frame.addWindowListener(Main.this);
				}
			});
		} catch (Exception ex)
		{
			JOptionPane.showMessageDialog(null, "An error occured initializing the game: " + ex);
			return;
		}

		Injector injector = Guice.createInjector(new EngineModule(assetSource));
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
		private final String m_assetSource;
		
		public EngineModule(String assetSource)
		{
			m_assetSource = assetSource;
		}
		
		@Override
		protected void configure()
		{
			bind(String.class).annotatedWith(Names.named("BASE_DIRECTORY")).toInstance(m_assetSource);
			bind(IInputSource.class).toInstance(FrameInputSource.create(m_frame));
			bind(IRenderer.class).toInstance(new FrameRenderer(m_frame, false, RenderFitMode.Frame));
			bind(IScriptFactory.class).toInstance(new NullScriptFactory());
			bind(IGameFactory.class).to(WorldBuilderFactory.class);
			bind(IAssetStreamFactory.class).toInstance(new BuilderAssetStreamFactory(m_assetSource));
			bind(IPhysicsWorldFactory.class).to(NullPhysicsWorldFactory.class);
			bind(IEntityFactory.class).to(NullEntityFactory.class);
			bind(IWorldFactory.class).to(EditorWorldFactory.class);
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
