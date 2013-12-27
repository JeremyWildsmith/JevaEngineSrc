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
package io.github.jevaengine.rpgbase.client.ui;

import io.github.jevaengine.Core;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Viewport;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent.EventType;
import io.github.jevaengine.math.Vector2D;

import java.awt.Color;
import java.awt.Graphics2D;

public abstract class ChatMenu extends Window
{
	private TextArea m_chatOutArea;
	private TextArea m_chatIn;

	public ChatMenu(UIStyle style)
	{
		super(style, 432, 180);
		this.setRenderBackground(false);

		final Sprite backgroundImage = Sprite.create(Core.getService(IResourceLibrary.class).openConfiguration("ui/chat.jsf"));

		backgroundImage.setAnimation("idle", AnimationState.Play);

		this.addControl(new Viewport(new IRenderable()
		{
			@Override
			public void render(Graphics2D g, int x, int y, float fScale)
			{
				backgroundImage.render(g, x, y, fScale);
			}
		}, 432, 180, false));

		m_chatOutArea = new TextArea(Color.green, 380, 80);
		m_chatOutArea.setLocation(new Vector2D(35, 70));
		m_chatOutArea.setRenderBackground(false);
		this.addControl(m_chatOutArea);

		m_chatIn = new TextArea(Color.white, 340, 20);
		m_chatIn.setEditable(true);
		m_chatIn.setLocation(new Vector2D(70, 152));
		m_chatIn.setRenderBackground(false);
		this.addControl(m_chatIn);
	}

	public void recieveChatMessage(String user, String message)
	{
		m_chatOutArea.appendText(user + ": " + message + "\n");
		m_chatOutArea.scrollToEnd();
	}

	@Override
	public void onKeyEvent(InputKeyEvent e)
	{
		if (e.keyChar == '\n')
		{
			if (e.type == EventType.KeyUp)
			{
				onSend(m_chatIn.getText());
				m_chatIn.setText("");
			}
		} else
			super.onKeyEvent(e);
	}

	public abstract void onSend(String message);

}
