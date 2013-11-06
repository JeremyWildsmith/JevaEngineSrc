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
package io.github.jevaengine.ui;

import java.awt.Color;

import io.github.jevaengine.Core;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.math.Vector2D;

public class DialogMenu extends Window
{

	public DialogMenu(UIStyle style, int width, int height)
	{
		super(style, width, height);
		this.setVisible(false);
	}

	public DialogMenu()
	{
		this(Core.getService(Game.class).getGameStyle(), 500, 200);
	}

	public void issueQuery(String query, String[] options, final IDialogResponder responder)
	{
		this.clearControls();

		TextArea textArea = new TextArea(query, Color.orange, this.getBounds().width - 20, this.getBounds().height - 60);
		textArea.setRenderBackground(false);
		textArea.setEffect(TextArea.DisplayEffect.Typewriter);

		textArea.setLocation(new Vector2D(10, 10));
		this.addControl(textArea);

		int xOffset = 10;
		for (final String option : options)
		{
			Button btn = new Button(option)
			{
				@Override
				public void onButtonPress()
				{
					DialogMenu.this.setVisible(false);
					responder.onAnswer(option);
				}
			};

			btn.setLocation(new Vector2D(xOffset, this.getBounds().height - 40));
			this.addControl(btn);

			xOffset += btn.getBounds().width + 15;
		}

		this.setVisible(true);
	}
}
