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
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.config.Variable;
import io.github.jevaengine.config.VariableStore;
import io.github.jevaengine.config.VariableValue;
import io.github.jevaengine.game.ResourceLoadingException;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.Item;
import io.github.jevaengine.rpgbase.ItemSlot;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.Item.ItemDescriptor;
import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.rpgbase.RpgCharacter.IRpgCharacterObserver;
import io.github.jevaengine.rpgbase.library.ResourceLibrary;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.WorldDirection;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SharedClass(name = "RpgCharacter", policy = SharePolicy.ClientR)
public class ServerRpgCharacter extends NetRpgCharacter implements IServerEntity
{
	private static final int SYNC_INTERVAL = 50;

	private ServerControlledRpgCharacter m_character;

	private String m_varStore;

	private Movement m_movementState;

	private Attack m_attackState;

	private int m_tickCount = 0;

	private ServerCommunicator m_owningCommunicator = null;

	private static AtomicInteger m_characterCount = new AtomicInteger();

	private ServerGame m_server;

	private String m_titleName;

	public ServerRpgCharacter(@Nullable String titleName, @Nullable String instanceName, ServerGame server, String store)
	{
		m_server = server;

		m_titleName = titleName;

		m_varStore = store;

		Variable root = VariableStore.create(Core.getService(ResourceLibrary.class).openResourceStream(m_varStore));

		m_character = new ServerControlledRpgCharacter(server, "__SERVERCHARACTER__" + (instanceName == null ? m_characterCount.getAndIncrement() : instanceName), root);

		m_character.addObserver(new ServerRpgCharacterObserver());

		m_movementState = new Movement(m_character.getLocation());
		m_attackState = new Attack(null);
	}

	public ServerRpgCharacter(@Nullable String titleName, @Nullable String instanceName, ServerGame server, List<VariableValue> arguments)
	{
		this(titleName, instanceName, server, argumentsCheck(arguments).get(0).getString());
	}

	public ServerRpgCharacter(@Nullable String instanceName, ServerGame server, List<VariableValue> arguments)
	{
		this(null, instanceName, server, argumentsCheck(arguments).get(0).getString());
	}

	public ServerRpgCharacter(@Nullable String instanceName, ServerGame server, String store)
	{
		this(null, instanceName, server, store);
	}

	private static List<VariableValue> argumentsCheck(List<VariableValue> arguments)
	{
		if (arguments.size() < 1)
			throw new ResourceLoadingException("Invalid number of arguments");

		return arguments;
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
					send(sender, new InitializationArguments(m_varStore, m_character.getName(), m_titleName));
					send(sender, m_movementState);
					send(sender, new HealthChange(m_character.getHealth()));

					for (ItemSlot slot : m_character.getInventory().getSlots())
					{
						if (!slot.isEmpty())
							send(sender, new AddItem(slot.getItem().getDescriptor()));
					}

					for (ItemSlot slot : m_character.getInventory().getGearSlots())
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

			if ((!visitor.requiresOwnership() || sender == m_owningCommunicator) && !visitor.isServerOnly())
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

	public static class ServerControlledRpgCharacter extends ControlledRpgCharacter
	{
		public ServerControlledRpgCharacter(ServerGame server, String name, Variable root)
		{
			super(name, root, new ServerCharacterBridge(server));
		}

		public static class ServerCharacterBridge extends ControlledRpgCharacterBridge<ControlledRpgCharacter>
		{
			private ServerGame m_server;

			public ServerCharacterBridge(ServerGame server)
			{
				m_server = server;
			}

			public void setWorld(String worldName)
			{
				ServerWorld world = m_server.getServerWorld(worldName);
				Entity entity = getMe();

				if (entity.isAssociated())
				{
					cancelTasks();
					entity.getWorld().removeEntity(entity);
				}

				world.getWorld().addEntity(entity);
			}
		}
	}

	private class ServerRpgCharacterObserver implements IRpgCharacterObserver
	{
		@Override
		public void movingTowards(@Nullable Vector2F target)
		{
			m_movementState = target == null ? new Movement(m_character.getLocation()) : new Movement(m_character.getLocation(), target);

			send(m_movementState);
		}

		@Override
		public void healthChanged(int health)
		{
			send(new HealthChange(health));
		}

		@Override
		public void placement(Vector2F location)
		{
			send(new Movement(location));
		}

		@Override
		public void enterWorld()
		{
			m_movementState = new Movement(ServerRpgCharacter.this.getCharacter().getLocation());
			m_server.entityEnter(ServerRpgCharacter.this);
		}

		@Override
		public void leaveWorld()
		{
			m_server.entityLeave(ServerRpgCharacter.this);
		}

		@Override
		public void attack(@Nullable RpgCharacter attackee)
		{
			m_attackState = new Attack(attackee == null ? null : attackee.getName());

			send(m_attackState);
		}

		@Override
		public void addItem(ItemDescriptor item)
		{
			send(new AddItem(item));
		}

		@Override
		public void removeItem(ItemDescriptor item)
		{
			send(new RemoveItem(item));
		}

		@Override
		public void equip(int slot)
		{
			send(new EquipItem(slot));
		}

		@Override
		public void equip(Item item)
		{
			send(new EquipItem(item.getDescriptor().toString()));
		}

		@Override
		public void unequip(ItemType gearType)
		{
			send(new UnequipItem(gearType));
		}

		@Override
		public void drop(int slot)
		{
			send(new DropItem(slot));
		}

		@Override
		public void loot(RpgCharacter accessor, int slot)
		{
			send(new LootItem(accessor.getName(), slot));
		}

		@Override
		public void consume(int slot)
		{
			send(new ConsumeItem(slot));
		}

		@Override
		public void onAttacked(RpgCharacter attacker)
		{
		}

		@Override
		public void taskBusyState(boolean isBusy)
		{
		}

		@Override
		public void moved(Vector2F delta)
		{
		}

		@Override
		public void directionChanged(WorldDirection direction)
		{
		}

		@Override
		public void die()
		{
		}

		@Override
		public void onDialogEvent(@Nullable Entity subject, int event)
		{
		}
	}
}
