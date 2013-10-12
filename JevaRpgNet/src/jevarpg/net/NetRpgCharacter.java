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
package jevarpg.net;

import jeva.communication.Communicator;
import jeva.communication.InvalidMessageException;
import jeva.communication.SharedEntity;
import jeva.config.Variable;
import jeva.math.Vector2F;
import jeva.world.MovementTask;
import jeva.world.World;
import jevarpg.AttackTask;
import jevarpg.Item;
import jevarpg.Item.ItemDescriptor;
import jevarpg.Item.ItemType;
import jevarpg.RpgCharacter;
import jevarpg.RpgCharacter.Inventory;

import com.sun.istack.internal.Nullable;

public abstract class NetRpgCharacter extends SharedEntity
{
	protected static class ControlledRpgCharacter extends RpgCharacter
	{
		private NetCharacterMovementTask m_lastMoveTask = new NetCharacterMovementTask();
		private AttackTask m_lastAttackTask = createAttackTask();

		public <Y extends ControlledRpgCharacter, T extends ControlledRpgCharacterBridge<Y>> ControlledRpgCharacter(@Nullable String name, Variable root, T bridge)
		{
			super(name, root, bridge);
		}

		public ControlledRpgCharacter(@Nullable String name, Variable root)
		{
			super(name, root);
		}

		public ControlledRpgCharacter(Variable root)
		{
			this(null, root);
		}

		public RpgCharacter getCharacter()
		{
			return this;
		}

		public Inventory getControlledInventory()
		{
			return super.getInventory();
		}

		void commandMoveTo(Vector2F destination)
		{
			if (!isTaskActive(m_lastMoveTask))
			{
				cancelTasks();
				addTask(m_lastMoveTask);
			}

			m_lastMoveTask.setDestination(destination);
		}

		// Only accessible within this package
		// Keep away from inherting classes etc...
		void commandAttack(@Nullable RpgCharacter target)
		{
			if (target == null && isTaskActive(m_lastAttackTask))
				cancelTask(m_lastAttackTask);
			else
			{
				if (!isTaskActive(m_lastAttackTask))
				{
					cancelTasks();
					addTask(m_lastAttackTask);
				}

				m_lastAttackTask.setTarget(target);
			}
		}

		private class NetCharacterMovementTask extends MovementTask
		{
			private Vector2F m_destination;

			public NetCharacterMovementTask()
			{
				super(new CharacterRouteTraveler());
				m_destination = new Vector2F();
			}

			public NetCharacterMovementTask(Vector2F destination)
			{
				super(new CharacterRouteTraveler());
				m_destination = destination;
			}

			public void setDestination(Vector2F destination)
			{
				m_destination = destination;
			}

			@Override
			protected void blocking()
			{
				// Server knows best, ignore blocking state.
			}

			@Override
			protected Vector2F getDestination()
			{
				return m_destination;
			}

			@Override
			protected boolean atDestination()
			{
				return getLocation().equals(m_destination);
			}

			@Override
			protected boolean hasNext()
			{
				return false;
			}
		}

		public static class ControlledRpgCharacterBridge<Y extends ControlledRpgCharacter> extends RpgCharacterBridge<Y>
		{

		}
	}

	protected static enum PrimitiveQuery
	{
		Initialize,
	}

	protected static class InitializationArguments
	{
		private String m_varStore;

		private String m_name;

		private String m_titleName;

		@SuppressWarnings("unused")
		// Used by Kryo
		private InitializationArguments()
		{
		}

		public InitializationArguments(String varStore, @Nullable String name, @Nullable String titleName)
		{
			m_varStore = varStore;
			m_name = name;
			m_titleName = titleName;
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

		public String getStore()
		{
			return m_varStore;
		}
	}

	protected interface IRpgCharacterVisitor
	{
		void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException;

		boolean requiresOwnership();

		boolean isServerOnly();
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

		public boolean isMoving()
		{
			return !m_location.equals(m_destination);
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller)
		{
			RpgCharacter character = controller.getCharacter();

			if (character.getLocation().difference(getLocation()).getLength() > 0.8F)
				character.setLocation(getLocation());

			if (isMoving())
			{
				if (!character.getLocation().equals(getDestination()))
					controller.commandMoveTo(getDestination());

			} else
			{
				if (!character.getLocation().equals(getLocation()))
					controller.commandMoveTo(getLocation());
			}
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return true;
		}
	}

	protected static final class Attack implements IRpgCharacterVisitor
	{
		@Nullable
		private String m_target;

		public Attack()
		{
		}

		public Attack(@Nullable String target)
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
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			if (m_target == null)
			{
				controller.commandAttack(null);
			} else
			{
				if (controller.getWorld().variableExists(m_target))
				{
					Object o = controller.getWorld().getVariable(m_target);

					if (o instanceof RpgCharacter)
						controller.commandAttack((RpgCharacter) o);
					else
						throw new InvalidMessageException(sender, this, "Server ordered client character to attack non-character target");
				} else
					throw new InvalidMessageException(sender, this, "Server ordered client character to attack non-existant target");
			}
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}

	protected static final class HealthChange implements IRpgCharacterVisitor
	{
		private int m_health;

		public HealthChange()
		{
			m_health = 0;
		}

		public HealthChange(int health)
		{
			m_health = health;
		}

		public int getHealth()
		{
			return m_health;
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			controller.getCharacter().setHealth(m_health);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return true;
		}
	}

	protected static class AddItem implements IRpgCharacterVisitor
	{
		private String m_item;

		@SuppressWarnings("unused")
		// Used by Kryo
		private AddItem()
		{
		}

		public AddItem(ItemDescriptor item)
		{
			m_item = item.toString();
		}

		public ItemDescriptor getItem()
		{
			return new ItemDescriptor(m_item);
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			if (!controller.getControlledInventory().addItem(new ItemDescriptor(m_item)))
				throw new InvalidMessageException(sender, this, "Characters inventory not in sync with server.");
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return true;
		}
	}

	protected static class RemoveItem implements IRpgCharacterVisitor
	{
		private String m_item;

		@SuppressWarnings("unused")
		// Used by Kryo
		private RemoveItem()
		{
		}

		public RemoveItem(ItemDescriptor item)
		{
			m_item = item.toString();
		}

		public ItemDescriptor getItem()
		{
			return new ItemDescriptor(m_item);
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			if (!controller.getControlledInventory().removeItem(new ItemDescriptor(m_item)))
				throw new InvalidMessageException(sender, this, "Characters inventory not in sync with server.");
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return true;
		}
	}

	protected static class LootItem implements IRpgCharacterVisitor
	{
		private String m_accessor;

		private int m_slot;

		@SuppressWarnings("unused")
		// Used by Kryo
		private LootItem()
		{
		}

		public LootItem(String accessor, int slot)
		{
			m_accessor = accessor;
			m_slot = slot;
		}

		public String getAccessor()
		{
			return m_accessor;
		}

		public int getSlot()
		{
			return m_slot;
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			if (!controller.getCharacter().isAssociated())
				throw new InvalidMessageException(sender, this, "Character unassociated with world. Cannot loot items.");

			World world = controller.getCharacter().getWorld();

			if (!world.variableExists(m_accessor))
				throw new InvalidMessageException(sender, this, "Accessor does not exist.");

			Variable v = controller.getCharacter().getWorld().getVariable(m_accessor);

			if (!(v instanceof RpgCharacter))
				throw new InvalidMessageException(sender, this, "Accessor is not an RpgCharacter.");

			Inventory inventory = controller.getControlledInventory();

			if (m_slot >= inventory.getSlots().length || m_slot < 0)
				throw new InvalidMessageException(sender, this, "Invalid inventory slot index");

			if (!controller.getControlledInventory().loot((RpgCharacter) v, inventory.getSlots()[m_slot]))
				throw new InvalidMessageException(sender, this, "Inventories are not in sync.");
		}

		@Override
		public boolean requiresOwnership()
		{
			return false;
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}

	protected static class ConsumeItem implements IRpgCharacterVisitor
	{
		private int m_slot;

		@SuppressWarnings("unused")
		// Used by Kryo
		private ConsumeItem()
		{
		}

		public ConsumeItem(int slot)
		{
			m_slot = slot;
		}

		public int getSlot()
		{
			return m_slot;
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			Inventory inventory = controller.getControlledInventory();

			if (m_slot >= inventory.getSlots().length || m_slot < 0)
				throw new InvalidMessageException(sender, this, "Invalid inventory slot index");

			inventory.consume(inventory.getSlots()[m_slot]);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}

	protected static class DropItem implements IRpgCharacterVisitor
	{
		private int m_slot;

		@SuppressWarnings("unused")
		// Used by Kryo
		private DropItem()
		{
		}

		public DropItem(int slot)
		{
			m_slot = slot;
		}

		public int getSlot()
		{
			return m_slot;
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			Inventory inventory = controller.getControlledInventory();

			if (m_slot >= inventory.getSlots().length || m_slot < 0)
				throw new InvalidMessageException(sender, this, "Invalid inventory slot index");

			inventory.drop(inventory.getSlots()[m_slot]);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}

	protected static class EquipItem implements IRpgCharacterVisitor
	{
		private int m_slot;
		private String m_item;

		@SuppressWarnings("unused")
		// Used by Kryo
		private EquipItem()
		{
		}

		public EquipItem(int slot)
		{
			m_slot = slot;
		}

		public EquipItem(String item)
		{
			m_item = item;
		}

		public int getSlot()
		{
			return m_slot;
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			Inventory inventory = controller.getControlledInventory();

			if (m_item != null)
			{
				inventory.equip(Item.create(m_item));
			} else
			{
				if (m_slot >= inventory.getSlots().length || m_slot < 0)
					throw new InvalidMessageException(sender, this, "Invalid inventory slot index");

				inventory.equip(inventory.getSlots()[m_slot]);
			}
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return m_item != null;
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
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			Inventory inventory = controller.getControlledInventory();

			if (!inventory.unequip(m_slot))
				throw new InvalidMessageException(sender, this, "Character inventory is out of sync.");
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}

	protected static class MoveTo implements IRpgCharacterVisitor
	{
		private Vector2F m_destination;

		@SuppressWarnings("unused")
		// Used by Kryo
		private MoveTo()
		{
		}

		public MoveTo(Vector2F destination)
		{
			m_destination = destination;
		}

		public Vector2F getDestination()
		{
			return m_destination;
		}

		@Override
		public void visit(Communicator sender, ControlledRpgCharacter controller) throws InvalidMessageException
		{
			controller.commandMoveTo(m_destination);
		}

		@Override
		public boolean requiresOwnership()
		{
			return true;
		}

		@Override
		public boolean isServerOnly()
		{
			return false;
		}
	}
}
