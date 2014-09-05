/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.rpgbase.demo.demos.Demo0;
import io.github.jevaengine.rpgbase.demo.demos.Demo2;
import io.github.jevaengine.rpgbase.demo.demos.Demo3;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.world.IWorldFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jeremy
 */
public class DemoMenu implements IState
{
	private IStateContext m_context;
	private Window m_window;
	
	private final IWindowFactory m_windowFactory;
	private final IWorldFactory m_worldFactory;
	private final IAudioClipFactory m_audioClipFactory;
	
	private final Logger m_logger = LoggerFactory.getLogger(DemoMenu.class);
	
	public DemoMenu(IWindowFactory windowFactory, IWorldFactory worldFactory, IAudioClipFactory audioClipFactory)
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
			m_window = m_windowFactory.create("ui/windows/demomenu.jwl", new MainMenuBehaviourInjector());
			context.getWindowManager().addWindow(m_window);
			m_window.center();
		} catch (AssetConstructionException e)
		{
			m_logger.error("Error constructing demo menu window", e);
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

	public void update(int iDelta) { }
	
	public class MainMenuBehaviourInjector extends WindowBehaviourInjector
	{	
		@Override
		public void doInject() throws NoSuchControlException
		{
			getControl(Button.class, "btnDemo0").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_context.setState(new Demo0(m_windowFactory, m_worldFactory, m_audioClipFactory));
				}
			});
			
			getControl(Button.class, "btnDemo1").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					//m_context.setState(new Demo1());
				}
			});
			
			getControl(Button.class, "btnDemo2").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_context.setState(new Demo2(m_windowFactory, m_worldFactory, m_audioClipFactory));
				}
			});
			
			getControl(Button.class, "btnDemo3").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_context.setState(new Demo3(m_windowFactory, m_worldFactory, m_audioClipFactory));
				}
			});
		}
	}
}
