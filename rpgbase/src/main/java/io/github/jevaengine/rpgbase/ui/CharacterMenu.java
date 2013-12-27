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

import java.awt.Color;
import java.awt.Graphics2D;
import java.lang.ref.WeakReference;

import io.github.jevaengine.Core;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.ParticleEmitter;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.MenuStrip;
import io.github.jevaengine.ui.Panel;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Viewport;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.MenuStrip.IMenuStripListener;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.EventType;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.IItemStore;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.rpgbase.ItemSlot;
import io.github.jevaengine.rpgbase.Loadout;


public class CharacterMenu extends Window
{
	private StatisticGuage m_healthGuage;
	private StatisticGuage m_experienceGuage;
	private Label m_lblLevel;
	private Label m_lblRank;
	private Label m_lblField;
	private MenuStrip m_contextStrip;

	private ParticleEmitter m_characterEmitter;

	private WeakReference<RpgCharacter> m_target;

	public CharacterMenu(UIStyle style)
	{
		super(style, 300, 330);

		m_characterEmitter = ParticleEmitter.create(Core.getService(IResourceLibrary.class).openConfiguration("particle/characterMenu/characterMenu.jpar"));

		m_characterEmitter.setEmit(true);

		m_contextStrip = new MenuStrip();
		m_healthGuage = new StatisticGuage(new Vector2D(), new Color(255, 0, 0, 122), 280, 8, 1.0F);
		m_experienceGuage = new StatisticGuage(new Vector2D(), new Color(39, 174, 30, 122), 280, 8, 1.0F);

		m_lblLevel = new Label("Level:", Color.orange);
		m_lblRank = new Label("Rank:", Color.orange);
		m_lblField = new Label("Field:", Color.orange);

		this.setVisible(false);

		this.addControl(new GearContainer(ItemType.HeadArmor), new Vector2D(22, 145));
		this.addControl(new GearContainer(ItemType.BodyArmor), new Vector2D(62, 145));
		this.addControl(new GearContainer(ItemType.Weapon), new Vector2D(102, 145));
		this.addControl(new GearContainer(ItemType.Accessory), new Vector2D(142, 145));

		this.addControl(new Label("Character Menu", Color.orange), new Vector2D(20, 10));
		this.addControl(new Label("Equipment", Color.orange), new Vector2D(10, 95));

		this.addControl(m_healthGuage, new Vector2D(10, 50));
		this.addControl(m_experienceGuage, new Vector2D(10, 60));
		this.addControl(m_lblLevel, new Vector2D(10, 195));
		this.addControl(m_lblRank, new Vector2D(10, 225));
		this.addControl(m_lblField, new Vector2D(10, 255));

		this.addControl(new Viewport(new IRenderable()
		{

			@Override
			public void render(Graphics2D g, int x, int y, float fScale)
			{
				m_characterEmitter.render(g, x + 30, y + 40, fScale * 3.0F);
				if (m_target.get() != null)
					m_target.get().getGraphic().render(g, x + 30, y + 60, fScale * 1.3F);
			}
		}, 75, 100), new Vector2D(215, 95));

		this.addControl(new Button("Close")
		{

			@Override
			public void onButtonPress()
			{
				CharacterMenu.this.setVisible(false);
			}
		}, new Vector2D(95, 290));

		m_target = new WeakReference<RpgCharacter>(null);
	}

	public CharacterMenu()
	{
		this(Core.getService(Game.class).getGameStyle());
	}

	public void showCharacter(RpgCharacter player)
	{
		this.setVisible(true);
		m_healthGuage.setValue((float) player.getHealth() / (float) player.getMaxHealth());
		m_experienceGuage.setValue(0.65F);
		m_lblLevel.setText("Level: 1");
		m_lblRank.setText("Rank: Private");
		m_lblField.setText("Field: Combatant");
		m_target = new WeakReference<RpgCharacter>(player);
	}

	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);

		if (m_target.get() != null)
		{
			m_healthGuage.setValue((float) m_target.get().getHealth() / (float) m_target.get().getMaxHealth());
			m_experienceGuage.setValue(0.65F);
		} else
			this.setVisible(false);
	}

	private class GearContainer extends Panel
	{
		private ItemType m_gearType;

		public GearContainer(ItemType type)
		{
			super(30, 30);
			this.setRenderBackground(false);
			m_gearType = type;
		}

		@Override
		public void onMouseEvent(InputMouseEvent mouseEvent)
		{
			if(m_target.get() == null)
				return;
			
			Loadout loadout = m_target.get().getLoadout();
			
			final ItemSlot gearSlot = loadout.getSlot(m_gearType);
			
			if (gearSlot == null || gearSlot.isEmpty())
				return;

			if (mouseEvent.type == EventType.MouseClicked && mouseEvent.mouseButton == MouseButton.Right)
			{
				CharacterMenu.this.addControl(m_contextStrip, mouseEvent.location.difference(CharacterMenu.this.getAbsoluteLocation()));

				String[] slotActions = gearSlot.getSlotActions(m_target.get());
				
				m_contextStrip.setContext(slotActions, new IMenuStripListener()
				{
					@Override
					public void onCommand(String command)
					{
						if (m_target.get() != null)
						{
							gearSlot.doSlotAction(m_target.get(), command);
						}
					}
				});
			}
		}

		@Override
		public void onKeyEvent(InputKeyEvent keyEvent)
		{
		}

		@Override
		public void update(int deltaTime)
		{
			if (this.isVisible())
				m_characterEmitter.update(deltaTime);
		}

		@Override
		public void render(Graphics2D g, int x, int y, float fScale)
		{
			g.setColor(Color.DARK_GRAY);
			g.drawRect(x, y, 20, 20);

			Sprite icon = m_gearType.getIcon();
			
			if(icon != null)
				icon.render(g, x + 10, y + 10, fScale);
			
			if (m_target.get() != null)
			{
				ItemSlot gearSlot = m_target.get().getLoadout().getSlot(m_gearType);
				
				if (gearSlot != null && !gearSlot.isEmpty())
					gearSlot.getItem().getGraphic().render(g, x + 10, y + 10, fScale);
			}

			super.render(g, x, y, fScale);
		}
	}
}
