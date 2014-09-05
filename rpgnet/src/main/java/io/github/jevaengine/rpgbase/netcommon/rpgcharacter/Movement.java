package io.github.jevaengine.rpgbase.netcommon.rpgcharacter;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.character.RpgCharacter;

public final class Movement implements INetVisitor<RpgCharacter>
{
	private Vector3F m_location;
	private Vector3F m_destination;

	public Movement()
	{
		m_location = new Vector3F();
		m_destination = new Vector3F();
	}

	public Movement(Vector3F location)
	{
		m_location = location;
		m_destination = location;
	}

	public Movement(Vector3F location, Vector3F destination)
	{
		m_location = location;
		m_destination = destination;
	}

	public Vector3F getLocation()
	{
		return m_location;
	}

	public Vector3F getDestination()
	{
		return m_destination;
	}

	@Override
	public void visit(Communicator sender, RpgCharacter character, boolean onServer) throws InvalidMessageException
	{
		if (character.getLocation().difference(getLocation()).getLength() > 0.8F)
			character.setLocation(getLocation());
		else
			character.moveTo(getDestination());
	}
	
	@Override
	public Class<RpgCharacter> getHostComponentClass()
	{
		return RpgCharacter.class;
	}
}