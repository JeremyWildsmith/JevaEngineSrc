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

import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.EventType;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.IItemStore;
import io.github.jevaengine.rpgbase.ItemSlot;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.MenuStrip;
import io.github.jevaengine.ui.MenuStrip.IMenuStripListener;
import io.github.jevaengine.ui.Panel;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;

import java.awt.Graphics2D;
import java.lang.ref.WeakReference;

public class InventoryMenu extends Window
{
	private IItemStore m_inventoryHost;
	private WeakReference<RpgCharacter> m_accessor;
	private MenuStrip m_menuStrip;

	public InventoryMenu(UIStyle style)
	{
		super(style, 240, 100);
		this.setVisible(false);

		m_accessor = new WeakReference<RpgCharacter>(null);

		m_menuStrip = new MenuStrip();
	}

	public void accessInventory(IItemStore host, RpgCharacter accessor)
	{
		this.clearControls();

		this.setVisible(true);

		m_inventoryHost = host;
		m_accessor = new WeakReference<RpgCharacter>(accessor);

		int x = 15;
		int y = 15;

		ItemSlot[] slots = m_inventoryHost.getSlots();

		for (int i = 0; i < slots.length; i++)
		{
			InventorySlotContainer slot = new InventorySlotContainer(slots[i]);
			slot.setRenderBackground(false);
			slot.setStyle(getStyle());

			if (x + slot.getBounds().width > this.getBounds().width)
			{
				y += 15 + slot.getBounds().height;
				x = 15;
			}

			this.addControl(slot, new Vector2D(x, y));

			x += 15 + (slot.getBounds().width);
		}

		this.setHeight(y + 70);

		this.addControl(new Button("Exit")
		{

			@Override
			public void onButtonPress()
			{
				InventoryMenu.this.setVisible(false);

			}
		}, new Vector2D(70, y + 35));
	}

	@Override
	public void update(int delta)
	{
		super.update(delta);

		// If the accessor is disposed of, the inventory host can be released.
		if (m_accessor.get() == null)
			m_inventoryHost = null;

		if (this.isVisible() && m_accessor.get() == null)
			this.setVisible(false);
	}

	private class InventorySlotContainer extends Panel
	{
		private ItemSlot m_slot;

		public InventorySlotContainer(ItemSlot slot)
		{
			super(30, 30);
			m_slot = slot;
		}

		@Override
		public void onMouseEvent(InputMouseEvent mouseEvent)
		{
			if (m_accessor.get() == null)
				return;

			if (mouseEvent.type == EventType.MouseClicked && mouseEvent.mouseButton == MouseButton.Right)
			{
				InventoryMenu.this.addControl(m_menuStrip, mouseEvent.location.difference(InventoryMenu.this.getAbsoluteLocation()));

				String options[] = m_slot.getSlotActions(m_accessor.get());

				if (options.length > 0)
				{
					m_menuStrip.setContext(options, new IMenuStripListener()
					{
						@Override
						public void onCommand(String command)
						{
							if (m_accessor.get() != null)
								m_slot.doSlotAction(m_accessor.get(), command);
						}
					});
				}
			}
		}

		@Override
		public void onKeyEvent(InputKeyEvent keyEvent)
		{
			// TODO Auto-generated method stub

		}

		@Override
		public void update(int deltaTime)
		{
		}

		@Override
		public void render(Graphics2D g, int x, int y, float fScale)
		{
			super.render(g, x, y, fScale);

			if (!m_slot.isEmpty())
				m_slot.getItem().getGraphic().render(g, x + this.getBounds().width / 3, y + this.getBounds().height / 3, fScale);
		}
	}
}
