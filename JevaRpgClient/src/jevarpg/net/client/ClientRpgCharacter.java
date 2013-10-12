package jevarpg.net.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.IResourceLibrary;
import jeva.communication.Communicator;
import jeva.communication.InvalidMessageException;
import jeva.communication.SharePolicy;
import jeva.communication.SharedClass;
import jeva.communication.SharedEntity;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.game.Game;
import jeva.graphics.IRenderable;
import jeva.graphics.Text;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.IWorldAssociation;
import jeva.world.World;
import jevarpg.ItemSlot;
import jevarpg.RpgCharacter;
import jevarpg.Item.ItemDescriptor;
import jevarpg.Item.ItemType;
import jevarpg.net.NetRpgCharacter;

@SharedClass(name = "RpgCharacter", policy = SharePolicy.ClientR)
public final class ClientRpgCharacter extends NetRpgCharacter implements IWorldAssociation, IClientShared
{
	private static final int SYNC_INTERVAL = 10;

	private volatile ClientControlledRpgCharacter m_character;

	private volatile boolean m_isLoading = false;

	private boolean dispatchedInit = false;

	private int m_tickCount = 0;

	private World m_world;

	public ClientRpgCharacter()
	{
	}

	@Override
	public SharedEntity getSharedEntity()
	{
		return this;
	}

	protected RpgCharacter getCharacter()
	{
		return m_character;
	}

	@Override
	public boolean isAssociated()
	{
		if (m_character != null)
			return m_character.isAssociated();
		else
			return false;
	}

	@Override
	public void disassociate()
	{
		if (m_character != null && m_character.isAssociated())
			m_character.getWorld().removeEntity(m_character);

		m_world = null;
	}

	@Override
	public void associate(World world)
	{
		m_world = world;

		if (m_character != null)
			m_world.addEntity(m_character);
	}

	@Override
	public void update(int deltaTime)
	{
		if (!dispatchedInit)
		{
			dispatchedInit = true;
			send(PrimitiveQuery.Initialize);
		}

		if (m_isLoading && m_character != null)
		{
			if (m_world != null)
				m_world.addEntity(m_character);

			m_isLoading = false;
		}

		m_tickCount += deltaTime;
		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object message) throws InvalidMessageException
	{
		if (message instanceof InitializationArguments)
		{
			final InitializationArguments args = (InitializationArguments) message;

			if (m_isLoading || m_character != null)
				throw new InvalidMessageException(sender, message, "Server dispatched initilization twice");

			m_isLoading = true;

			new Thread()
			{
				@Override
				public void run()
				{
					if (args.getName() == null)
						m_character = new ClientControlledRpgCharacter(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(args.getStore())), args.getTitleName());
					else
						m_character = new ClientControlledRpgCharacter(args.getName(), VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(args.getStore())), args.getTitleName());
				}
			}.start();

			return true;
		} else if (m_character == null || !isAssociated())
			return false;

		if (message instanceof PrimitiveQuery)
		{
			switch ((PrimitiveQuery) message)
			{
			case Initialize:
				throw new InvalidMessageException(sender, message, "Invalid message recieved from server.");
			default:
				throw new InvalidMessageException(sender, message, "Unrecognized message recieved from server.");
			}
		} else if (message instanceof IRpgCharacterVisitor)
			((IRpgCharacterVisitor) message).visit(sender, m_character);
		else
			throw new InvalidMessageException(sender, message, "Unrecognized message recieved from server.");

		return true;
	}

	public interface ClientRpgCharacterObserver
	{

	}

	public class ClientControlledRpgCharacter extends ControlledRpgCharacter
	{
		private Text m_titleText;

		public ClientControlledRpgCharacter(@Nullable String name, Variable root, @Nullable String titleName)
		{
			super(name, root);

			if (titleName != null)
				m_titleText = new Text(titleName, new Vector2D(0, 0), Core.getService(Game.class).getGameStyle().getFont(Color.red), 1.0F);
		}

		public ClientControlledRpgCharacter(Variable root, @Nullable String titleName)
		{
			this(null, root, titleName);
		}

		@Override
		public IRenderable[] getGraphics()
		{
			if (m_titleText == null)
				return super.getGraphics();

			ArrayList<IRenderable> graphics = new ArrayList<IRenderable>();

			graphics.addAll(Arrays.asList(super.getGraphics()));

			graphics.add(m_titleText);

			return graphics.toArray(new IRenderable[graphics.size()]);
		}

		@Override
		public Inventory getInventory()
		{
			return new ServerInventory(super.getInventory());
		}

		@Override
		public void moveTo(Vector2D destination)
		{
			send(new MoveTo(new Vector2F(destination)));
		}

		@Override
		public void attack(RpgCharacter target)
		{
			send(new Attack(target.getName()));
		}

		public class ServerInventory extends Inventory
		{
			private Inventory m_inventory;

			public ServerInventory(Inventory inventory)
			{
				m_inventory = inventory;
			}

			@Override
			public ItemSlot[] getSlots()
			{
				return m_inventory.getSlots();
			}

			@Override
			public boolean isFull()
			{
				return m_inventory.isFull();
			}

			@Override
			public ItemSlot getGearSlot(ItemType gearType)
			{
				return m_inventory.getGearSlot(gearType);
			}

			@Override
			public int getSlotIndex(ItemSlot slot)
			{
				return m_inventory.getSlotIndex(slot);
			}

			@Override
			public boolean addItem(ItemDescriptor item)
			{
				if (isFull())
					return false;

				ClientRpgCharacter.this.send(new AddItem(item));

				return true;
			}

			@Override
			public boolean removeItem(ItemDescriptor item)
			{
				if (!hasItem(item))
					return false;

				send(new RemoveItem(item));
				return true;
			}

			@Override
			public void equip(ItemSlot slot)
			{
				send(new EquipItem(getSlotIndex(slot)));
			}

			@Override
			public boolean unequip(ItemType gearType)
			{
				ItemSlot gearSlot = getGearSlot(gearType);

				if (gearSlot.isEmpty() || isFull())
					return false;

				send(new UnequipItem(gearType));

				return true;
			}

			@Override
			public void consume(ItemSlot slot)
			{
				send(new ConsumeItem(getSlotIndex(slot)));
			}

			@Override
			public boolean loot(RpgCharacter accessor, ItemSlot slot)
			{
				if (slot.isEmpty() || accessor.getInventory().isFull())
					return false;

				send(new LootItem(accessor.getName(), getSlotIndex(slot)));

				return true;
			}

			@Override
			public void drop(ItemSlot slot)
			{
				send(new DropItem(getSlotIndex(slot)));
			}
		}
	}
}
