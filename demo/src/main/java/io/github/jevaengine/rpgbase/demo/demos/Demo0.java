/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo.demos;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.NullIInitializationProgressMonitor;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.game.ControlledCamera;
import io.github.jevaengine.game.ICamera;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.rpgbase.demo.DemoMenu;
import io.github.jevaengine.rpgbase.demo.IState;
import io.github.jevaengine.rpgbase.demo.IStateContext;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.world.IWorldFactory;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.scene.isometric.TopologicalIsometricSceneBufferFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeremy
 */
public class Demo0 implements IState
{
	private static final String DEMO_MAP = "world/demo0.jmp";
	private static final String DEMO_VIEW_WINDOW = "ui/windows/demoview.jwl";
	
	private static final int TILE_WIDTH = 64;
	private static final int TILE_HEIGHT = 32;
	
	private IStateContext m_context;
	private World m_world;
	private Window m_window;
	
	private final IWindowFactory m_windowFactory;
	private final IWorldFactory m_worldFactory;
	private final IAudioClipFactory m_audioClipFactory;
	
	private final Logger m_logger = LoggerFactory.getLogger(Demo0.class);
	
	public Demo0(IWindowFactory windowFactory, IWorldFactory worldFactory, IAudioClipFactory audioClipFactory)
	{
		m_windowFactory = windowFactory;
		m_worldFactory = worldFactory;
		m_audioClipFactory = audioClipFactory;
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		try
		{
			m_world = m_worldFactory.create(DEMO_MAP, 1.0F, 1.0F, new NullIInitializationProgressMonitor());
			ControlledCamera camera = new ControlledCamera(new TopologicalIsometricSceneBufferFactory(TILE_WIDTH, TILE_HEIGHT));
			camera.lookAt(new Vector3F(12F, 12F, 0F));
			camera.attach(m_world);
			m_window = m_windowFactory.create(DEMO_VIEW_WINDOW, new DemoWindowBehaviourInjector(camera));
			context.getWindowManager().addWindow(m_window);	
			
			m_window.center();
		} catch (AssetConstructionException e)
		{
			m_logger.error("Error occured constructing demo world or world view. Reverting to MainMenu.", e);
			m_context.setState(new DemoMenu(m_windowFactory, m_worldFactory, m_audioClipFactory));
		}
	}

	public void leave()
	{
		if(m_window != null)
		{
			m_context.getWindowManager().removeWindow(m_window);
			m_window.dispose();
		}
	}

	public void update(int deltaTime)
	{
		m_world.update(deltaTime);
	}
	
	private class DemoWindowBehaviourInjector extends WindowBehaviourInjector
	{
		private final ICamera m_camera;
		
		public DemoWindowBehaviourInjector(ICamera camera)
		{
			m_camera = camera;
		}
		
		@Override
		public void doInject() throws NoSuchControlException
		{
			final WorldView demoWorldView = getControl(WorldView.class, "demoWorldView");
			
			getControl(Button.class, "btnBack").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_context.setState(new DemoMenu(m_windowFactory, m_worldFactory, m_audioClipFactory));
				}
			});
			
			demoWorldView.setCamera(m_camera);
		}
	}
}
