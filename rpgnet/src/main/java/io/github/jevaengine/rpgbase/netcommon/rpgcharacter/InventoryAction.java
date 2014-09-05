package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.netcommon.entity.NetEntityIdentifier;
import io.github.jevaengine.rpgbase.DefaultItemSlot;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.world.entity.DefaultEntity;

public final class InventoryAction implements INetVisitor<RpgCharacter>
{
	private NetEntityIdentifier m_accessor;
	private String m_action;
	private int m_slotIndex;
	
	@SuppressWarnings("unused")
	// Used by Kryo
	private InventoryAction()
	{
	}

	public InventoryAction(RpgCharacter accessor, String action, int slotIndex)
	{
		m_accessor = new NetEntityIdentifier(accessor.getInstanceName());
		m_action = action;
		m_slotIndex = slotIndex;
	}

	public String getAction()
	{
		return m_action;
	}

	@Override
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{
		DefaultEntity accessor = m_accessor == null ? character : character.getWorld().getEntities().getByName(m_accessor.get(onServer));

		if(!(accessor instanceof RpgCharacter))
			throw new InvalidMessageException(sender, this, "Invalid accessor.");
		
		DefaultItemSlot[] slots = character.getInventory().getSlots();
		
		if(m_slotIndex > slots.length || m_slotIndex < 0)
			throw new InvalidMessageException(sender, this, "Invalid slot index");
		
		slots[m_slotIndex].doSlotAction((RpgCharacter)accessor, m_action);
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}