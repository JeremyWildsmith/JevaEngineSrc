/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.Core;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.demo.demos.Demo0;
import io.github.jevaengine.rpgbase.demo.demos.Demo1;
import io.github.jevaengine.rpgbase.demo.demos.Demo2;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.Window;

/**
 *
 * @author Jeremy
 */
public class MainMenu implements IState
{
	private IStateContext m_context;
	private Window m_demoSelection;
	
	public MainMenu()
	{
		m_demoSelection = new Window(Core.getService(Game.class).getGameStyle(), 300, 500);
		m_demoSelection.setRenderBackground(false);
		
		m_demoSelection.setLocation(new Vector2D(100,100));
		m_demoSelection.setMovable(false);
		
		m_demoSelection.addControl(new Button("0. AI Wondering Demo")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Demo0());
			}
		}, new Vector2D(20, 50));
		
		m_demoSelection.addControl(new Button("1. Basic Interaction Demo")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Demo1());
			}
		}, new Vector2D(20, 90));
		
		m_demoSelection.addControl(new Button("2. Dialogue Interaction")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Demo2());
			}
		}, new Vector2D(20, 130));
		
		m_demoSelection.addControl(new Button("Credits")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Credits());
			}
		}, new Vector2D(20, 170));
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		Core.getService(IWindowManager.class).addWindow(m_demoSelection);
	}

	public void leave()
	{
		Core.getService(IWindowManager.class).removeWindow(m_demoSelection);
	}

	public void update(int iDelta)
	{
		
	}
	
}
