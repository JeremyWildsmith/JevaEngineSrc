/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.demo.demos.Demo0;
import io.github.jevaengine.rpgbase.demo.demos.Demo1;
import io.github.jevaengine.rpgbase.demo.demos.Demo2;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;

/**
 *
 * @author Jeremy
 */
public class MainMenu implements IState
{
	private IStateContext m_context;
	private Window m_demoSelection;
	
	public MainMenu(final UIStyle style)
	{
		m_demoSelection = new Window(style, 300, 500);
		
		m_demoSelection.setLocation(new Vector2D(100,100));
		m_demoSelection.setMovable(false);
		m_demoSelection.setRenderBackground(false);
		
		m_demoSelection.addControl(new Button("0. AI Wondering Demo")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Demo0(style));
			}
		}, new Vector2D(20, 50));
		
		m_demoSelection.addControl(new Button("1. Basic Interaction Demo")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Demo1(style));
			}
		}, new Vector2D(20, 90));
		
		m_demoSelection.addControl(new Button("2. Dialogue Interaction")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Demo2(style));
			}
		}, new Vector2D(20, 130));
		
		m_demoSelection.addControl(new Button("Credits")
		{
			@Override
			public void onButtonPress()
			{
				m_context.setState(new Credits(style));
			}
		}, new Vector2D(20, 170));
	}
	
	public void enter(IStateContext context)
	{
		m_context = context;
		context.getWindowManager().addWindow(m_demoSelection);
	}

	public void leave()
	{
		m_context.getWindowManager().removeWindow(m_demoSelection);
	}

	public void update(int iDelta)
	{
		
	}
	
}
