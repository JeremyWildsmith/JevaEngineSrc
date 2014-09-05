package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.character.RpgCharacter;

public final class HealthSet implements INetVisitor<RpgCharacter>
{
	private int m_health;

	public HealthSet()
	{
		m_health = 0;
	}

	public HealthSet(int health)
	{
		m_health = health;
	}

	public int getHealth()
	{
		return m_health;
	}

	@Override
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{
		character.setHealth(m_health);
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}