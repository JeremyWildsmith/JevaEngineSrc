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
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.Inventory;
import io.github.jevaengine.rpgbase.Inventory.InventorySlot;
import io.github.jevaengine.rpgbase.Item;
import io.github.jevaengine.rpgbase.ItemSlot;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.Item.ItemIdentifer;
import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.rpgbase.Loadout;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;

import java.util.concurrent.atomic.AtomicInteger;

@SharedClass(name = "RpgCharacter", policy = SharePolicy.ClientR)
public class ServerRpgCharacter extends NetRpgCharacter implements IServerEntity
{
	private static final int SYNC_INTERVAL = 50;

	private RpgCharacter m_character;
	
	private String m_config;

	private Movement m_movementState;
	private Attack m_attackState;

	private int m_tickCount = 0;

	private static AtomicInteger m_characterCount = new AtomicInteger();

	private String m_titleName;
	
	private ServerCommunicator m_owningCommunicator = null;

	public ServerRpgCharacter(@Nullable String titleName, @Nullable String instanceName, String store, @Nullable ServerCommunicator owningCommunicator)
	{
		m_titleName = titleName;
		m_config = store;
		m_owningCommunicator = owningCommunicator;

		IVariable root = Core.getService(IResourceLibrary.class).openConfiguration(m_config);

		m_character = new RpgCharacter("__SERVERCHARACTER__" + (instanceName == null ? m_characterCount.getAndIncrement() : instanceName),
										root,
										new ServerCharacterBridge());

		ServerRpgCharacterObserver observer = new ServerRpgCharacterObserver();
		
		m_character.addObserver(observer);
		m_character.addActionObserver(observer);
		m_character.addConditionObserver(observer);
		
		m_character.getInventory().addObserver(new ServerRpgCharacterInventoryObserver());
		m_character.getLoadout().addObserver(new ServerRpgCharacterLoadoutObserver());

		m_movementState = new Movement(m_character.getLocation());
		m_attackState = new Attack(null);
	}
	
	public ServerRpgCharacter(@Nullable String titleName, @Nullable String instanceName, String store)
	{
		this(titleName, instanceName, store, null);
	}
	
	public ServerRpgCharacter(@Nullable String instanceName, String store)
	{
		this(null, instanceName, store, null);
	}
	
	public void setOwner(ServerCommunicator owner)
	{
		m_owningCommunicator = owner;
	}

	@Override
	public void update(int deltaTime)
	{
		m_tickCount += deltaTime;

		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
	}

	@Override
	public Entity getControlledEntity()
	{
		return m_character;
	}

	@Override
	public SharedEntity getSharedEntity()
	{
		return this;
	}

	public RpgCharacter getCharacter()
	{
		return m_character;
	}

	@Override
	protected synchronized boolean onMessageRecieved(Communicator sender, Object message) throws InvalidMessageException
	{
		ServerCommunicator communicator = (ServerCommunicator) sender;

		if (message instanceof PrimitiveQuery)
		{
			switch ((PrimitiveQuery) message)
			{
				case Initialize:
					send(sender, new InitializationArguments(m_config, m_character.getInstanceName(), m_titleName, m_owningCommunicator == sender));
					send(sender, m_movementState);
					send(sender, new HealthSet(m_character.getHealth()));

					for (InventorySlot slot : m_character.getInventory().getSlots())
					{
						if (!slot.isEmpty())
							send(sender, new AddInventoryItem(slot.getIndex(), slot.getItem().getDescriptor()));
					}

					for (ItemSlot slot : m_character.getLoadout().getSlots())
					{
						if (!slot.isEmpty())
							send(sender, new EquipItem(slot.getItem().getDescriptor().toString()));
					}
					
					break;
				default:
					throw new InvalidMessageException(sender, message, "Unrecognized message");
			}
		} else if (message instanceof IRpgCharacterVisitor)
		{
			IRpgCharacterVisitor visitor = (IRpgCharacterVisitor) message;

			if ((!visitor.requiresOwnership() || sender == m_owningCommunicator) && !visitor.isServerDispatchOnly())
			{
				try
				{
					((IRpgCharacterVisitor) message).visit(sender, m_character);
				} catch (InvalidMessageException e)
				{
					communicator.disconnect("Visitor synchronization error: " + e.toString());
				}
			} else
				communicator.disconnect("Attempted to mutate entity without sufficient access.");
		} else
			throw new InvalidMessageException(sender, message, "Unrecognized message");

		return true;
	}
	
	public static class ServerCharacterBridge extends RpgCharacter.RpgCharacterBridge<RpgCharacter>
	{
		public void setWorld(String worldName)
		{
			ServerWorld world = Core.getService(ServerGame.class).getServerWorld(worldName);
			Entity entity = getEntity();

			if (entity.isAssociated())
			{
				cancelTasks();
				entity.getWorld().removeEntity(entity);
			}

			world.getWorld().addEntity(entity);
		}
	}

	private class ServerRpgCharacterObserver implements RpgCharacter.IActionObserver,
														RpgCharacter.IConditionObserver,
														RpgCharacter.IEntityObserver
	{
		@Override
		public void healthChanged(int delta)
		{
			send(new HealthSet(m_character.getHealth()));
		}

		@Override
		public void replaced()
		{
			m_movementState = new Movement(getCharacter().getLocation());
			send(m_movementState);
		}

		@Override
		public void enterWorld()
		{
			m_movementState = new Movement(ServerRpgCharacter.this.getCharacter().getLocation());
			Core.getService(ServerGame.class).entityEnter(ServerRpgCharacter.this);
		}

		@Override
		public void leaveWorld()
		{
			Core.getService(ServerGame.class).entityLeave(ServerRpgCharacter.this);
		}

		@Override
		public void attack(@Nullable RpgCharacter attackee)
		{
			m_movementState = new Movement(m_character.getLocation());
			m_attackState = new Attack(attackee == null ? null : attackee.getInstanceName());
			send(m_movementState);
			send(m_attackState);
		}

		@Override
		public void endMoving()
		{
			m_movementState = new Movement(getCharacter().getLocation());
			send(m_movementState);
		}

		@Override
		public void movingTowards(Vector2F target)
		{
			m_movementState = new Movement(m_character.getLocation(), target);
			send(m_movementState);
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
			send(new AddInventoryItem(iSlot, item));
		}

		@Override
		public void removeItem(int iSlot, ItemIdentifer item)
		{
			send(new RemoveInventoryItem(iSlot));
		}

		@Override
		public void itemAction(int slotIndex, RpgCharacter accessor, String action) { }
	}
	
	private class ServerRpgCharacterLoadoutObserver implements Loadout.ILoadoutObserver
	{
		@Override
		public void unequip(ItemType gearType)
		{
			send(new UnequipItem(gearType));
		}

		@Override
		public void equip(Item item)
		{
			send(new EquipItem(item.getDescriptor().toString()));
		}
		
	}
}
