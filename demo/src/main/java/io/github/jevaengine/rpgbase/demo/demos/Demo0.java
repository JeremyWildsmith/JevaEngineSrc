/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo.demos;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.game.ControlledCamera;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.demo.IState;
import io.github.jevaengine.rpgbase.demo.IStateContext;
import io.github.jevaengine.rpgbase.demo.MainMenu;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.world.World;

/**
 *
 * @author Jeremy
 */
public class Demo0 implements IState
{
	private static final String DEMO_MAP = "world/demo0.jmp";
	
	private IStateContext m_context;
	private World m_world;
	private Window m_window;
	
	public Demo0(final UIStyle style)
	{
		m_world = World.create(Core.getService(ResourceLibrary.class).openConfiguration(DEMO_MAP));

		m_window = new Window(style, 420, 500);
		m_window.setLocation(new Vector2D(100, 100));
		
		ControlledCamera camera = new ControlledCamera(new Vector2F(2.5F, 2.5F));
		camera.attach(m_world);
		
		WorldView worldViewport = new WorldView(400, 400);
		worldViewport.setRenderBackground(false);
		worldViewport.setCamera(camera);
		
		m_window.addControl(worldViewport, new Vector2D(10,70));
		
		m_window.addControl(new Button("Go Back")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new MainMenu(style));
			}
		}, new Vector2D(10,10));
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		context.getWindowManager().addWindow(m_window);
	}

	public void leave()
	{
		m_context.getWindowManager().removeWindow(m_window);
	}

	public void update(int iDelta)
	{
		m_world.update(iDelta);
	}
	
}
