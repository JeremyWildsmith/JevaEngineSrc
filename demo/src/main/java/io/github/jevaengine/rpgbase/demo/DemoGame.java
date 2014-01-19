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
package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.RpgGame;
import io.github.jevaengine.rpgbase.demo.demos.Demo2;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.util.Nullable;

public class DemoGame extends RpgGame implements IStateContext
{
	@Nullable private RpgCharacter m_player;

	private UIStyle m_style;
	private Sprite m_cursor;
	
	private IState m_state;
	
	@Override
	protected void startup()
	{
		super.startup();
		
		ResourceLibrary library = Core.getService(ResourceLibrary.class);
		
		m_style = UIStyle.create(library.openConfiguration("ui/game.juis"));
		
		m_cursor = Sprite.create(library.openConfiguration("ui/tech/cursor/cursor.jsf"));
		m_cursor.setAnimation("idle", AnimationState.Play);
		
		m_state = new Demo2();
		m_state.enter(this);
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		m_state.update(deltaTime);
	}

	@Override
	public void setPlayer(@Nullable RpgCharacter player)
	{
		m_player = player;
	}

	@Override
	public RpgCharacter getPlayer()
	{
		return m_player;
	}

	@Override
	public UIStyle getGameStyle()
	{
		return m_style;
	}

	@Override
	protected Sprite getCursor()
	{
		return m_cursor;
	}
	
	public void setState(IState state)
	{
		m_state.leave();
		m_state = state;
		state.enter(this);
	}
	
	@Override
	public void mouseButtonStateChanged(InputMouseEvent e) { }
}
