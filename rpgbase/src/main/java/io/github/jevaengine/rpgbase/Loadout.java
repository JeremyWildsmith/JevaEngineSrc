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


package io.github.jevaengine.rpgbase;

import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import java.util.ArrayList;

/**
 *
 * @author Jeremy
 */
public final class Loadout
{
	private ArrayList<GearSlot> m_slots = new ArrayList<GearSlot>();
	
	private RpgCharacter m_owner;
	
	private Observers m_observers = new Observers();
	
	public Loadout(RpgCharacter owner)
	{
		m_owner = owner;
		m_slots.add(new GearSlot(ItemType.Weapon));
		m_slots.add(new GearSlot(ItemType.Accessory));
		m_slots.add(new GearSlot(ItemType.BodyArmor));
		m_slots.add(new GearSlot(ItemType.HeadArmor));
	}
	
	public void addObserver(ILoadoutObserver observer)
	{
		m_observers.add(observer);
	}
	
	public void removeObserver(ILoadoutObserver observer)
	{
		m_observers.remove(observer);
	}
	
	@Nullable
	public GearSlot getSlot(ItemType gearType)
	{
		for(GearSlot s : m_slots)
			if(s.getGearType() == gearType)
				return s;
		
		return null;
	}
	
	@Nullable
	public GearSlot[] getSlots()
	{
		return m_slots.toArray(new GearSlot[m_slots.size()]);
	}
	
	@Nullable
	public Item equip(Item item)
	{
		Item prev = null;
		
		for(GearSlot s : m_slots)
		{
			if(s.getGearType() == item.getType())
			{
				prev = s.isEmpty() ? null : s.getItem();
				s.setItem(item);
				break;
			}
		}
		
		return prev;
	}
	
	public Item unequip(ItemType type)
	{
		Item prev = null;
		
		for(GearSlot s : m_slots)
		{
			if(s.getGearType() == type)
			{
				prev = s.isEmpty() ? null : s.getItem();
				s.clear();
				break;
			}
		}
		
		return prev;
	}
	
	public void clearGear()
	{
		for(ItemSlot s : m_slots)
			s.clear();
	}

	private static class Observers extends StaticSet<ILoadoutObserver>
	{
		public void unequip(ItemType gearType)
		{
			for(ILoadoutObserver o : this)
				o.unequip(gearType);
		}
		
		public void equip(Item item)
		{
			for(ILoadoutObserver o : this)
				o.equip(item);
		}
	}
	
	public interface ILoadoutObserver
	{
		void unequip(ItemType gearType);
		void equip(Item item);
	}
	
	public final class GearSlot extends ItemSlot
	{
		private Item.ItemType m_gearType;
		private RpgCharacter m_owner;

		public GearSlot(ItemType gearType)
		{
			m_gearType = gearType;
		}

		public ItemType getGearType()
		{
			return m_gearType;
		}
		
		private void unequip(@Nullable ItemSlot destination)
		{
			if (!isEmpty() && (destination == null || destination.isEmpty()))
			{
				if (destination != null)
					destination.setItem(getItem());

				clear();
			}
		}
		
		private void unequip()
		{
			unequip(null);
		}

		@Override
		public void clear()
		{
			if(!isEmpty())
				m_observers.unequip(m_gearType);
			
			super.clear();
		}
		
		@Override
		public Item setItem(Item item)
		{
			Item prevItem = super.setItem(item);
			
			m_observers.equip(item);
			return prevItem;
		}
		
		@Override
		public String[] getSlotActions(RpgCharacter accessor)
		{
			if (isEmpty())
				return new String[0];

			if (accessor == m_owner)
			{
				switch (getItem().getType())
				{
					case Accessory:
					case BodyArmor:
					case HeadArmor:
					case Weapon:
						return new String[]
						{
							"Unequip",
							"Drop"
						};
					default:
						throw new RuntimeException("Unknown Item Type");
				}
			}

			return new String[0];
		}

		@Override
		public void doSlotAction(RpgCharacter accessor, String action)
		{
			if (action.equals("Unequip"))
				unequip(m_owner.getInventory().getEmptySlot());
			else if(action.equals("Drop"))
				unequip();
		}
	}

}
