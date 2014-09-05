package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.ItemType;
import io.github.jevaengine.rpgbase.character.CharacterLoadout;
import io.github.jevaengine.rpgbase.character.RpgCharacter;

public final class UnequipItem implements INetVisitor<RpgCharacter>
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
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{		
		CharacterLoadout loadout = character.getLoadout();
		loadout.unequip(m_slot);
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}