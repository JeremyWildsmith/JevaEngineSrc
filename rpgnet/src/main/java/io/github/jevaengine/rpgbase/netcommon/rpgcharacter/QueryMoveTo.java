package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.character.RpgCharacter;

public final class QueryMoveTo implements INetVisitor<RpgCharacter>
{
	private Vector3F m_destination;

	@SuppressWarnings("unused")
	// Used by Kryo
	private QueryMoveTo()
	{
	}

	public QueryMoveTo(Vector3F destination)
	{
		m_destination = destination;
	}

	public Vector3F getDestination()
	{
		return m_destination;
	}

	@Override
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{
		character.moveTo(m_destination);
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}