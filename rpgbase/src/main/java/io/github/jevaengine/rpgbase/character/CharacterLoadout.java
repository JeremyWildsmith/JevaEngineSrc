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


package io.github.jevaengine.rpgbase.character;

import io.github.jevaengine.rpgbase.ILoadout;
import io.github.jevaengine.rpgbase.ILoadoutObserver;
import io.github.jevaengine.rpgbase.Item;
import io.github.jevaengine.rpgbase.DefaultItemSlot;
import io.github.jevaengine.rpgbase.ItemType;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;

import java.util.HashMap;

/**
 *
 * @author Jeremy
 */
public final class CharacterLoadout implements ILoadout
{
	private HashMap<ItemType, DefaultItemSlot> m_slots = new HashMap<>();
	
	private Observers m_observers = new Observers();
	
	public CharacterLoadout()
	{
		m_slots.put(ItemType.Weapon, new DefaultItemSlot());
		m_slots.put(ItemType.Accessory, new DefaultItemSlot());
		m_slots.put(ItemType.BodyArmor, new DefaultItemSlot());
		m_slots.put(ItemType.BodyArmor, new DefaultItemSlot());
	}
	
	@Override
	public void addObserver(ILoadoutObserver observer)
	{
		m_observers.add(observer);
	}
	
	@Override
	public void removeObserver(ILoadoutObserver observer)
	{
		m_observers.remove(observer);
	}
	
	@Override
	@Nullable
	public DefaultItemSlot getSlot(ItemType gearType)
	{
		return m_slots.get(gearType);
	}
	
	@Override
	@Nullable
	public Item equip(Item item)
	{
		DefaultItemSlot targetSlot = m_slots.get(item.getType());
		
		if(targetSlot == null)
			return null;
		
		m_observers.equip(item);
		return targetSlot.setItem(item);
	}
	@Override
	public Item unequip(ItemType type)
	{
		m_observers.unequip(type);

		DefaultItemSlot targetSlot = m_slots.get(type);
		
		if(targetSlot == null)
			return null;
		
		return targetSlot.clear();
	}
	
	@Override
	public void clear()
	{
		for(DefaultItemSlot s : m_slots.values())
		{
			if(!s.isEmpty())
				unequip(s.getItem().getType());
		}
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
}
