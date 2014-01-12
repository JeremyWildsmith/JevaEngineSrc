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
package io.github.jevaengine.rpgbase.netcommon;

import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.Item;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.Item.ItemIdentifer;
import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.rpgbase.ItemSlot;
import io.github.jevaengine.rpgbase.Loadout;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;

import javax.script.ScriptException;

public final class NetRpgCharacter extends NetEntity
{	
	private static final RpgCharacter getCharacter(Entity e, Communicator sender, Object message) throws InvalidMessageException
	{
		if(!(e instanceof RpgCharacter))
			throw new InvalidMessageException(sender, message, "This visitor must operate on an rpg character.");
		
		return (RpgCharacter)e;
	}
	
	public static final class DialogueEvent implements IEntityVisitor
	{
		private int m_eventCode;
		private @Nullable NetEntityName m_listenerEntity;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private DialogueEvent() { }
		
		public DialogueEvent(int eventCode, @Nullable String listenerEntity)
		{
			m_eventCode = eventCode;
			m_listenerEntity = new NetEntityName(listenerEntity);
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			if(!character.isAssociated())
				throw new InvalidMessageException(sender, this, "Unassociated entity cannot invoke dialogue event.");
		
			Entity listener = m_listenerEntity == null ? null : character.getWorld().getEntity(m_listenerEntity.get(onServer));
			
			try
			{
				character.getScript().invokeScriptFunction("remoteDialogueEvent", character.getScriptBridge(), m_eventCode, listener == null ? null : listener.getScriptBridge());
			} catch (NoSuchMethodException ex){
			} catch (ScriptException ex)
			{
				throw new CoreScriptException(ex);
			}
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return false;
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}
	}
	
	public static final class Movement implements IEntityVisitor
	{
		private Vector2F m_location;
		private Vector2F m_destination;

		public Movement()
		{
			m_location = new Vector2F();
			m_destination = new Vector2F();
		}

		public Movement(Vector2F location)
		{
			m_location = location;
			m_destination = location;
		}

		public Movement(Vector2F location, Vector2F destination)
		{
			m_location = location;
			m_destination = destination;
		}

		public Vector2F getLocation()
		{
			return m_location;
		}

		public Vector2F getDestination()
		{
			return m_destination;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			if (character.getLocation().difference(getLocation()).getLength() > 0.8F)
				character.setLocation(getLocation());
			else
				character.moveTo(getDestination());
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}
	}

	public static final class Attack implements IEntityVisitor
	{
		private NetEntityName m_target;

		public Attack()
		{
		}

		public Attack(String target)
		{
			m_target = new NetEntityName(target);
		}

		public boolean isAttacking()
		{
			return m_target != null;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			Entity target = character.getWorld().getEntity(m_target.get(onServer));
			
			if (target != null)
			{
				if (target instanceof RpgCharacter)
					character.attack((RpgCharacter) target);
				else
					throw new InvalidMessageException(sender, this, "Server ordered client character to attack non-character target");
			} else
				throw new InvalidMessageException(sender, this, "Server ordered client character to attack non-existant target");

		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return false;
		}
	}

	public final static class HealthSet implements IEntityVisitor
	{
		private int m_health;

		public HealthSet()
		{
			m_health = 0;
		}

		public HealthSet(int health)
		{
			m_health = health;
		}

		public int getHealth()
		{
			return m_health;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			character.setHealth(m_health);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}
	}

	public final static class AddInventoryItem implements IEntityVisitor
	{
		private String m_item;
		private int m_slot;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private AddInventoryItem()
		{
		}

		public AddInventoryItem(int slot, ItemIdentifer item)
		{
			m_item = item.toString();
			m_slot = slot;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			ItemSlot[] slots = character.getInventory().getSlots();
			
			if (m_slot >= slots.length || m_slot < 0)
				throw new InvalidMessageException(sender, this, "Inventory slot index is not valid.");
			else
				slots[m_slot].setItem(Item.create(m_item));
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}
	}

	public final static class RemoveInventoryItem implements IEntityVisitor
	{
		private int m_slot;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private RemoveInventoryItem()
		{
		}

		public RemoveInventoryItem(int slot)
		{
			m_slot = slot;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			ItemSlot[] slots = character.getInventory().getSlots();
			
			if (m_slot >= slots.length || m_slot < 0)
				throw new InvalidMessageException(sender, this, "Inventory slot index is not valid.");
			else
				slots[m_slot].clear();
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}
	}

	public final static class EquipItem implements IEntityVisitor
	{
		private String m_item;

		@SuppressWarnings("unused")
		// Used by Kryo
		private EquipItem()
		{
		}

		public EquipItem(String item)
		{
			m_item = item;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			character.getLoadout().equip(Item.create(m_item));
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}
	}

	public final static class UnequipItem implements IEntityVisitor
	{
		private ItemType m_slot;

		@SuppressWarnings("unused")
		// Used by Kryo
		private UnequipItem()
		{
		}

		public UnequipItem(ItemType slot)
		{
			m_slot = slot;
		}

		public ItemType getSlot()
		{
			return m_slot;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			Loadout loadout = character.getLoadout();
			loadout.unequip(m_slot);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return true;
		}
	}
	
	public final static class InventoryAction implements IEntityVisitor
	{
		private NetEntityName m_accessor;
		private String m_action;
		private int m_slotIndex;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private InventoryAction()
		{
		}

		public InventoryAction(RpgCharacter accessor, String action, int slotIndex)
		{
			m_accessor = new NetEntityName(accessor.getInstanceName());
			m_action = action;
			m_slotIndex = slotIndex;
		}

		public String getAction()
		{
			return m_action;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			Entity accessor = m_accessor == null ? character : character.getWorld().getEntity(m_accessor.get(onServer));

			if(!(accessor instanceof RpgCharacter))
				throw new InvalidMessageException(sender, this, "Invalid accessor.");
			
			ItemSlot[] slots = character.getInventory().getSlots();
			
			if(m_slotIndex > slots.length || m_slotIndex < 0)
				throw new InvalidMessageException(sender, this, "Invalid slot index");
			
			slots[m_slotIndex].doSlotAction((RpgCharacter)accessor, m_action);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return false;
		}
	}

	public final static class QueryMoveTo implements IEntityVisitor
	{
		private Vector2F m_destination;

		@SuppressWarnings("unused")
		// Used by Kryo
		private QueryMoveTo()
		{
		}

		public QueryMoveTo(Vector2F destination)
		{
			m_destination = destination;
		}

		public Vector2F getDestination()
		{
			return m_destination;
		}

		@Override
		public void visit(Communicator sender, Entity entity, boolean onServer) throws InvalidMessageException
		{
			RpgCharacter character = getCharacter(entity, sender, this);
			
			character.moveTo(m_destination);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerDispatchOnly()
		{
			return false;
		}
	}
}
