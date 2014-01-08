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
package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.Inventory;
import io.github.jevaengine.rpgbase.Inventory.InventorySlot;
import io.github.jevaengine.rpgbase.Item;
import io.github.jevaengine.rpgbase.ItemSlot;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.Item.ItemIdentifer;
import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.rpgbase.Loadout;
import io.github.jevaengine.rpgbase.RpgCharacter.RpgCharacterBridge;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter;
import io.github.jevaengine.util.Nullable;

@SharedClass(name = "RpgCharacter", policy = SharePolicy.ClientR)
public final class ServerRpgCharacter extends ServerEntity<RpgCharacter>
{
	private NetRpgCharacter.Movement m_movementState;
	private NetRpgCharacter.Attack m_attackState;
	
	//For Factory only...
	ServerRpgCharacter(RpgCharacter character, String configuration, @Nullable ServerCommunicator owner)
	{
		super(character, configuration, owner);
		
		ServerRpgCharacterObserver observer = new ServerRpgCharacterObserver();
		
		character.addObserver(observer);
		character.addActionObserver(observer);
		character.addConditionObserver(observer);
		
		character.getInventory().addObserver(new ServerRpgCharacterInventoryObserver());
		character.getLoadout().addObserver(new ServerRpgCharacterLoadoutObserver());

		m_movementState = new NetRpgCharacter.Movement(character.getLocation());
		m_attackState = new NetRpgCharacter.Attack(null);
	}
	
	public ServerRpgCharacter(String instanceName, String config, ServerCommunicator owner)
	{
		this(new RpgCharacter(instanceName, Core.getService(ResourceLibrary.class).openConfiguration(config)), config, owner);
	}
	
	public ServerRpgCharacter(String config, ServerCommunicator owner)
	{
		this(new RpgCharacter(Core.getService(ResourceLibrary.class).openConfiguration(config)), config, owner);	
	}
	
	@Override
	public void initializeRemote(Communicator sender)
	{
		RpgCharacter character = getEntity();
		
		send(sender, m_movementState);
		send(sender, new NetRpgCharacter.HealthSet(character.getHealth()));

		for (InventorySlot slot : character.getInventory().getSlots())
		{
			if (!slot.isEmpty())
				send(sender, new NetRpgCharacter.AddInventoryItem(slot.getIndex(), slot.getItem().getDescriptor()));
		}

		for (ItemSlot slot : character.getLoadout().getSlots())
		{
			if (!slot.isEmpty())
				send(sender, new NetRpgCharacter.EquipItem(slot.getItem().getDescriptor().toString()));
		}
	}

	private class ServerRpgCharacterObserver implements RpgCharacter.IActionObserver,
														RpgCharacter.IConditionObserver,
														RpgCharacter.IEntityObserver
	{
		@Override
		public void healthChanged(int delta)
		{
			send(new NetRpgCharacter.HealthSet(getEntity().getHealth()));
		}

		@Override
		public void replaced()
		{
			m_movementState = new NetRpgCharacter.Movement(getEntity().getLocation());
			send(m_movementState);
		}

		@Override
		public void enterWorld()
		{
			m_movementState = new NetRpgCharacter.Movement(getEntity().getLocation());
		}

		@Override
		public void leaveWorld() { }

		@Override
		public void attack(@Nullable RpgCharacter attackee)
		{
			m_movementState = new NetRpgCharacter.Movement(getEntity().getLocation());
			m_attackState = new NetRpgCharacter.Attack(attackee == null ? null : attackee.getInstanceName());
			send(m_movementState);
			send(m_attackState);
		}

		@Override
		public void endMoving()
		{
			m_movementState = new NetRpgCharacter.Movement(getEntity().getLocation());
			send(m_movementState);
		}

		@Override
		public void movingTowards(Vector2F target)
		{
			m_movementState = new NetRpgCharacter.Movement(getEntity().getLocation(), target);
			send(m_movementState);
		}

		@Override
		public void flagSet(String name, int value)
		{
			
		}

		@Override
		public void flagCleared(String name)
		{
			
		}
		
		@Override
		public void beginMoving() { }

		@Override
		public void attacked(RpgCharacter attacker) { }
	}
	
	private class ServerRpgCharacterInventoryObserver implements Inventory.IInventoryObserver
	{
		@Override
		public void addItem(int iSlot, ItemIdentifer item)
		{
			send(new NetRpgCharacter.AddInventoryItem(iSlot, item));
		}

		@Override
		public void removeItem(int iSlot, ItemIdentifer item)
		{
			send(new NetRpgCharacter.RemoveInventoryItem(iSlot));
		}

		@Override
		public void itemAction(int slotIndex, RpgCharacter accessor, String action) { }
	}
	
	private class ServerRpgCharacterLoadoutObserver implements Loadout.ILoadoutObserver
	{
		@Override
		public void unequip(ItemType gearType)
		{
			send(new NetRpgCharacter.UnequipItem(gearType));
		}

		@Override
		public void equip(Item item)
		{
			send(new NetRpgCharacter.EquipItem(item.getDescriptor().toString()));
		}
	}
	
	public class ServerRpgCharacterScriptBridge extends RpgCharacterBridge
	{
		public void setWorld(String worldName)
		{
			ServerWorld world = Core.getService(ServerGame.class).getServerWorld(worldName);
			
			RpgCharacter entity = getEntity();

			if (entity.isAssociated())
			{
				cancelTasks();
				entity.getWorld().removeEntity(entity);
			}

			world.getWorld().addEntity(entity);
		}
	}
	
}
