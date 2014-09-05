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
package io.github.jevaengine.ui;

import io.github.jevaengine.graphics.IFont;
import io.github.jevaengine.graphics.IImmutableGraphic;
import io.github.jevaengine.graphics.NullFont;
import io.github.jevaengine.graphics.NullGraphic;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.joystick.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.ui.style.ComponentStateStyle;
import io.github.jevaengine.util.StaticSet;

import java.awt.Graphics2D;

public final class Button extends Control
{
	public static final String COMPONENT_NAME = "button";
	
	private String m_text;

	private ComponentState m_state = ComponentState.Default;

	private IFont m_font = new NullFont();
	private IImmutableGraphic m_frame = new NullGraphic();
	
	private Observers m_observers = new Observers();
	
	public Button(String text)
	{
		super(COMPONENT_NAME);
		m_text = text;
	}
	
	public Button(String instanceName, String text)
	{
		super(COMPONENT_NAME, instanceName);
		m_text = text;
	}
	
	public void addObserver(IButtonObserver o)
	{
		m_observers.add(o);
	}
	
	public void removeObserver(IButtonObserver o)
	{
		m_observers.remove(o);
	}
	
	private void enterState(ComponentState state)
	{
		m_state = state;
		
		ComponentStateStyle stateStyle = getComponentStyle().getStateStyle(m_state);
		stateStyle.playEnter();
		
		m_font = stateStyle.getFont();
		
		Rect2D textBounds = m_font.getTextBounds(m_text);
		m_frame = stateStyle.createFrame(textBounds.width, textBounds.height);
	}
	
	@Override
	protected void onEnter()
	{
		enterState(ComponentState.Enter);
	}

	@Override
	protected void onLeave()
	{
		enterState(ComponentState.Default);
	}

	public String getText()
	{
		return m_text;
	}
	
	public void setText(String text)
	{
		m_text = text;
		enterState(ComponentState.Default);
	}
	
	@Override
	public boolean onMouseEvent(InputMouseEvent mouseEvent)
	{
		if (mouseEvent.mouseButton == MouseButton.Left)
		{
			if(mouseEvent.type == InputMouseEvent.MouseEventType.MousePressed)
			{
				enterState(ComponentState.Activated);
			} else if(mouseEvent.type == InputMouseEvent.MouseEventType.MouseReleased)
			{
				enterState(ComponentState.Enter);
			} else if(mouseEvent.type == InputMouseEvent.MouseEventType.MouseClicked)
				m_observers.onPress();
		}
		
		return true;
	}

	@Override
	public boolean onKeyEvent(InputKeyEvent keyEvent)
	{
		return false;
	}

	@Override
	public void update(int deltaTime) { }
	
	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		m_frame.render(g, x, y, scale);
		
		Rect2D stringBounds = m_font.getTextBounds(m_text);

		int textAnchorX = m_frame.getBounds().width / 2 - stringBounds.width / 2;
		int textAnchorY = m_frame.getBounds().height / 2 - stringBounds.height / 2;
		
		m_font.drawText(g, textAnchorX + x, textAnchorY + y, scale, m_text);
	}

	protected void onStyleChanged()
	{
		enterState(m_state);
	}

	@Override
	public Rect2D getBounds()
	{
		return m_frame.getBounds();
	}

	public interface IButtonObserver
	{
		void onPress();
	}
	
	private class Observers extends StaticSet<IButtonObserver>
	{
		public void onPress()
		{
			for(IButtonObserver o : this)
				o.onPress();
		}
	}
}
