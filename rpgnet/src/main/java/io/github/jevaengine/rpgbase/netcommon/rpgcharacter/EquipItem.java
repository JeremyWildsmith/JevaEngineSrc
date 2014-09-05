package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.ItemIdentifier;
import io.github.jevaengine.rpgbase.character.RpgCharacter;

public final class EquipItem implements INetVisitor<RpgCharacter>
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
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{
		character.getLoadout().equip(new ItemIdentifier(m_item));
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}