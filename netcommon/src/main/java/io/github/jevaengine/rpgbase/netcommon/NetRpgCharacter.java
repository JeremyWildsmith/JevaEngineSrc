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
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.math.Vector2D;
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

public abstract class NetRpgCharacter extends SharedEntity
{
	protected static enum PrimitiveQuery
	{
		Initialize,
	}

	protected static class InitializationArguments
	{
		private String m_configuration;

		private String m_name;

		private String m_titleName;

		private boolean m_isClientOwned;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private InitializationArguments() { }

		public InitializationArguments(String config, @Nullable String name, @Nullable String titleName, boolean isClientOwned)
		{
			m_configuration = config;
			m_name = name;
			m_titleName = titleName;
			m_isClientOwned = isClientOwned;
		}

		@Nullable
		public String getTitleName()
		{
			return m_titleName;
		}

		@Nullable
		public String getName()
		{
			return m_name;
		}

		public String getConfiguration()
		{
			return m_configuration;
		}
		
		public boolean isClientOwned()
		{
			return m_isClientOwned;
		}
	}

	protected interface IRpgCharacterVisitor
	{
		void visit(Communicator sender, RpgCharacter character) throws InvalidMessageException;

		boolean isServerDispatchOnly();
		boolean requiresOwnership();
	}
	
	protected static final class DialogueEvent implements IRpgCharacterVisitor
	{
		private int m_eventCode;
		private @Nullable String m_speakerEntity;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private DialogueEvent() { }
		
		public DialogueEvent(int eventCode, @Nullable String speakerEntity)
		{
			m_eventCode = eventCode;
			m_speakerEntity = speakerEntity;
		}

		@Override
		public void visit(Communicator sender, RpgCharacter character) throws InvalidMessageException
		{
			if(!character.isAssociated())
				throw new InvalidMessageException(sender, this, "Unassociated entity cannot invoke dialogue event.");
		
			Entity speaker = m_speakerEntity == null ? null : character.getWorld().getEntity(m_speakerEntity);
			
			if(speaker == null)
				throw new InvalidMessageException(sender, this, "Unknown speaker entity.");
			try
			{
				speaker.getScript().invokeScriptFunction("remoteDialogueEvent", character.getScriptBridge(), m_eventCode);
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
	
	protected static final class Movement implements IRpgCharacterVisitor
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
		public void visit(Communicator sender, RpgCharacter character)
		{
			if (character.getLocation().difference(getLocation()).getLength() > 0.8F)
				character.setLocation(getLocation());
			else
				character.moveTo(getDestination().floor());
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

	protected static final class Attack implements IRpgCharacterVisitor
	{
		private String m_target;

		public Attack()
		{
		}

		public Attack(String target)
		{
			m_target = target;
		}

		public String getTarget()
		{
			return m_target;
		}

		public boolean isAttacking()
		{
			return m_target != null;
		}

		@Override
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			Entity target = controller.getWorld().getEntity(m_target);
			
			if (target != null)
			{
				if (target instanceof RpgCharacter)
					controller.attack((RpgCharacter) target);
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

	protected static final class HealthSet implements IRpgCharacterVisitor
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
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			controller.setHealth(m_health);
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

	protected static class AddInventoryItem implements IRpgCharacterVisitor
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
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			ItemSlot[] slots = controller.getInventory().getSlots();
			
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

	protected static class RemoveInventoryItem implements IRpgCharacterVisitor
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
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			ItemSlot[] slots = controller.getInventory().getSlots();
			
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

	protected static class EquipItem implements IRpgCharacterVisitor
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
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			controller.getLoadout().equip(Item.create(m_item));
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

	protected static class UnequipItem implements IRpgCharacterVisitor
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
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			Loadout loadout = controller.getLoadout();
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
	
	protected static class InventoryAction implements IRpgCharacterVisitor
	{
		private String m_accessor;
		private String m_action;
		private int m_slotIndex;
		
		@SuppressWarnings("unused")
		// Used by Kryo
		private InventoryAction()
		{
		}

		public InventoryAction(RpgCharacter accessor, String action, int slotIndex)
		{
			m_accessor = accessor.getInstanceName();
			m_action = action;
			m_slotIndex = slotIndex;
		}

		public String getAction()
		{
			return m_action;
		}

		@Override
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			Entity accessor = m_accessor == null ? controller : controller.getWorld().getEntity(m_accessor);

			if(accessor == null || !(accessor instanceof RpgCharacter))
				throw new InvalidMessageException(sender, this, "Invalid accessor.");
			
			ItemSlot[] slots = controller.getInventory().getSlots();
			
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

	protected static class QueryMoveTo implements IRpgCharacterVisitor
	{
		private Vector2D m_destination;

		@SuppressWarnings("unused")
		// Used by Kryo
		private QueryMoveTo()
		{
		}

		public QueryMoveTo(Vector2D destination)
		{
			m_destination = destination;
		}

		public Vector2D getDestination()
		{
			return m_destination;
		}

		@Override
		public void visit(Communicator sender, RpgCharacter controller) throws InvalidMessageException
		{
			controller.moveTo(m_destination);
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
