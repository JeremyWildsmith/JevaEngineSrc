/**
 * *****************************************************************************
 * Copyright (c) 2013 Jeremy. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * If you'd like to obtain a another license to this code, you may contact
 * Jeremy to discuss alternative redistribution options.
 *
 * Contributors: Jeremy - initial API and implementation
 *****************************************************************************
 */
package io.github.jevaengine.rpgbase;

import io.github.jevaengine.rpgbase.Item.ItemIdentifer;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author Jeremy
 */
public final class Inventory implements IItemStore
{

	private ArrayList<InventorySlot> m_inventory;

	private Observers m_observers = new Observers();

	private RpgCharacter m_owner;

	public Inventory(RpgCharacter owner, int slotCount)
	{
		m_owner = owner;

		m_inventory = new ArrayList<InventorySlot>(slotCount);

		for (int i = 0; i < slotCount; i++)
		{
			m_inventory.add(new InventorySlot(i));
		}
	}

	public void addObserver(IInventoryObserver observer)
	{
		m_observers.add(observer);
	}

	public void removeObserver(IInventoryObserver observer)
	{
		m_observers.remove(observer);
	}

	@Override
	public InventorySlot[] getSlots()
	{
		return m_inventory.toArray(new InventorySlot[m_inventory.size()]);
	}

	@Override
	public boolean hasItem(Item.ItemIdentifer item)
	{
		for (ItemSlot slot : m_inventory)
		{
			if (!slot.isEmpty() && slot.getItem().getDescription().equals(item))
				return true;
		}

		return false;
	}

	@Override
	public boolean addItem(ItemIdentifer item)
	{
		for (int i = 0; i < m_inventory.size(); i++)
		{
			if (m_inventory.get(i).isEmpty())
			{
				m_inventory.get(i).setItem(Item.create(item));
				m_observers.addItem(i, item);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean removeItem(ItemIdentifer item)
	{
		for (int i = 0; i < m_inventory.size(); i++)
		{
			ItemSlot slot = m_inventory.get(i);

			if (!slot.isEmpty() && slot.getItem().getDescription().equals(item))
			{
				m_observers.removeItem(i, item);
				slot.clear();
				return true;
			}
		}

		return false;
	}

	@Override
	@Nullable
	public InventorySlot getEmptySlot()
	{
		for (InventorySlot slot : m_inventory)
		{
			if (slot.isEmpty())
				return slot;
		}

		return null;
	}

	@Override
	public boolean isFull()
	{
		return getEmptySlot() == null;
	}
	
	public InventoryBridge getScriptBridge()
	{
		return new InventoryBridge();
	}

	public interface IInventoryObserver
	{

		void addItem(int slotIndex, ItemIdentifer item);

		void removeItem(int slotIndex, ItemIdentifer item);
		
		void itemAction(int slotIndex, RpgCharacter accessor, String action);
	}

	private static class Observers extends StaticSet<IInventoryObserver>
	{

		void addItem(int slotIndex, ItemIdentifer item)
		{
			for (IInventoryObserver o : this)
			{
				o.addItem(slotIndex, item);
			}
		}

		void removeItem(int slotIndex, ItemIdentifer item)
		{
			for (IInventoryObserver o : this)
			{
				o.removeItem(slotIndex, item);
			}
		}
		
		void itemAction(int slotIndex, RpgCharacter accessor, String action)
		{
			for (IInventoryObserver o : this)
			{
				o.itemAction(slotIndex, accessor, action);
			}
		}
	}

	public class InventoryBridge
	{
		public int addItem(String descriptor, int quantity)
		{
			int added;

			for (added = 0; added < quantity && Inventory.this.addItem(new ItemIdentifer(descriptor)); added++);

			return added;
		}

		public boolean hasItem(String descriptor, int quantity)
		{
			Item.ItemIdentifer searchingFor = new ItemIdentifer(descriptor);

			int count = 0;

			for (ItemSlot slot : Inventory.this.getSlots())
			{
				if (!slot.isEmpty() && slot.getItem().getDescriptor().equals(searchingFor))
					count++;
			}

			return (count >= quantity);
		}

		public int removeItem(String descriptor, int quantity)
		{
			int taken = 0;

			for (taken = 0; taken < quantity && Inventory.this.removeItem(new ItemIdentifer(descriptor)); taken++);

			return taken;
		}
	}
	
	public final class InventorySlot extends ItemSlot
	{
		private int m_slotIndex;

		public InventorySlot(int slotIndex)
		{
			m_slotIndex = slotIndex;
		}

		public int getIndex()
		{
			return m_slotIndex;
		}
		
		private boolean loot(RpgCharacter accessor)
		{
			if (isEmpty())
				return false;

			for (ItemSlot accessorSlot : accessor.getInventory().getSlots())
			{
				if (accessorSlot.isEmpty())
				{
					accessorSlot.setItem(getItem());

					m_observers.removeItem(m_slotIndex, getItem().getDescriptor());

					clear();

					return true;
				}
			}

			return false;
		}

		private void drop()
		{
			if (isEmpty())
				return;

			m_observers.removeItem(m_slotIndex, getItem().getDescriptor());
			clear();
		}

		private void equip()
		{
			ItemSlot gearSlot = m_owner.getLoadout().getSlot(getItem().getType());

			if (gearSlot != null)
			{
				Item prev = gearSlot.setItem(getItem());

				if (prev != null)
					setItem(prev);

				m_observers.removeItem(m_slotIndex, null);
			}
		}

		@Override
		public void clear()
		{
			if (!isEmpty())
				m_observers.removeItem(m_inventory.indexOf(this), getItem().getDescriptor());
			
			super.clear();
		}

		@Override
		public String[] getSlotActions(RpgCharacter accessor)
		{
			if (isEmpty())
				return new String[0];

			if (accessor == m_owner)
			{
				ArrayList<String> commands = new ArrayList<String>();
				
				Collections.addAll(commands, getItem().getCommands());
				
				commands.add("Drop");
				
				if(getItem().getType().isWieldable())
					commands.add("Equip");
				
				return commands.toArray(new String[commands.size()]);
			} else if (m_owner.isDead())
				return new String[] { "Loot" };

			return new String[0];
		}

		@Override
		public void doSlotAction(RpgCharacter accessor, String action)
		{
			if (isEmpty())
				return;

			if (action.equals("Drop"))
				drop();
			else if (action.equals("Loot"))
				loot(accessor);
			else if (action.equals("Equip"))
				equip();
			else
				getItem().doCommand(accessor, this, action);
			
			m_observers.itemAction(m_slotIndex, accessor, action);
		}
	}
}
