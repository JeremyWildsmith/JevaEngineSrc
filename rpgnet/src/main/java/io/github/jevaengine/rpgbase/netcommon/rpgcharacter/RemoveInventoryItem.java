package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.DefaultItemSlot;
import io.github.jevaengine.rpgbase.character.RpgCharacter;

public final class RemoveInventoryItem implements INetVisitor<RpgCharacter>
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
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{
		DefaultItemSlot[] slots = character.getInventory().getSlots();
		
		if (m_slot >= slots.length || m_slot < 0)
			throw new InvalidMessageException(sender, this, "Inventory slot index is not valid.");
		else
			slots[m_slot].clear();
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}