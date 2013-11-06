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
package io.github.jevaengine.rpgbase.ui;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Viewport;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.ItemSlot;

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.ref.WeakReference;

public class ItemInfoMenu extends Window
{
	private WeakReference<ItemSlot> m_slot;

	private TextArea m_itemDescription;
	private Label m_itemName;

	public ItemInfoMenu(UIStyle style)
	{
		super(style, 200, 300);

		this.setVisible(false);

		m_slot = new WeakReference<ItemSlot>(null);
		m_itemDescription = new TextArea("", Color.white, 180, 200);
		m_itemName = new Label("", Color.red);

		this.addControl(m_itemDescription, new Vector2D(10, 50));
		this.addControl(m_itemName, new Vector2D(40, 10));

		this.addControl(new Button("Close")
		{
			@Override
			public void onButtonPress()
			{
				ItemInfoMenu.this.setVisible(false);
			}
		}, new Vector2D(70, 265));

		Viewport rifleViewport = new Viewport(new IRenderable()
		{
			@Override
			public void render(Graphics2D g, int x, int y, float fScale)
			{
				if (m_slot.get() != null)
					m_slot.get().getItem().getGraphic().render(g, x + 15, y + 15, fScale);
			}
		}, 80, 20);
		rifleViewport.setRenderBackground(false);

		this.addControl(rifleViewport, new Vector2D(10, 10));
	}

	public void showItem(ItemSlot slot)
	{
		if (slot.isEmpty())
			return;

		m_itemName.setText("Name: " + slot.getItem().getName());
		m_slot = new WeakReference<ItemSlot>(slot);
		m_itemDescription.setText(slot.getItem().getDescription().length() <= 0 ? "No description" : slot.getItem().getDescription());

		this.setVisible(true);
	}

	@Override
	public void update(int deltaTime)
	{
		if (this.isVisible() && (m_slot.get() == null || m_slot.get().isEmpty()))
			this.setVisible(false);

		super.update(deltaTime);
	}
}
