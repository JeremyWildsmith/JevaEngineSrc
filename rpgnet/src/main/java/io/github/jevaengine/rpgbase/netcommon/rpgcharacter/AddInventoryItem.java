package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.IItemSlot;
import io.github.jevaengine.rpgbase.ItemIdentifier;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.rpgbase.character.RpgCharacter.RpgCharacterController;

public final class AddInventoryItem implements INetVisitor<RpgCharacterController>
{
	private String m_item;
	private int m_slot;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private AddInventoryItem()
	{
	}

	public AddInventoryItem(int slot, ItemIdentifier item)
	{
		m_item = item.toString();
		m_slot = slot;
	}

	@Override
	public void visit(Communicator sender, RpgCharacterController controller, boolean onServer) throws InvalidMessageException
	{
		IItemSlot[] slots = controller.getInventory().getSlots();
		
		if (m_slot >= slots.length || m_slot < 0)
			throw new InvalidMessageException(sender, this, "Inventory slot index is not valid.");
		else
			slots[m_slot].setItem(new ItemIdentifier(m_item));
	}

	@Override
	public Class<RpgCharacterController> getHostComponentClass()
	{
		return RpgCharacterController.class;
	}
}