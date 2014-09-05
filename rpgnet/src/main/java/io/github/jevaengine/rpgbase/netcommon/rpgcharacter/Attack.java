package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.netcommon.entity.NetEntityIdentifier;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.world.entity.DefaultEntity;

public final class Attack implements INetVisitor<RpgCharacter>
{
	private NetEntityIdentifier m_target;

	public Attack()
	{
	}

	public Attack(String target)
	{
		m_target = new NetEntityIdentifier(target);
	}

	public boolean isAttacking()
	{
		return m_target != null;
	}

	@Override
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{	
		DefaultEntity target = character.getWorld().getEntities().getByName(m_target.get(onServer));
		
		if (target != null)
		{
			if (target instanceof RpgCharacter)
				character.attack((RpgCharacter) target);
			else
				throw new InvalidMessageException(sender, this, "Server ordered client character to attack non-character target");
		} else
			throw new InvalidMessageException(sender, this, "Server ordered client character to attack non-existant target");
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}