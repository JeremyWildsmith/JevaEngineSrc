package jeva.communication;

public class EntityId
{
	private boolean m_isOwned;

	private long m_id;

	@SuppressWarnings("unused")
	// Used by Kryo
	private EntityId()
	{
	}

	public EntityId(boolean isOwned, long objectId)
	{
		m_isOwned = isOwned;
		m_id = objectId;
	}

	public long getId()
	{
		return m_id;
	}

	public boolean isOwned()
	{
		return m_isOwned;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + (m_isOwned ? 1231 : 1237);
		result = prime * result + (int) (m_id ^ (m_id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EntityId other = (EntityId) obj;
		if (m_isOwned != other.m_isOwned)
			return false;
		if (m_id != other.m_id)
			return false;
		return true;
	}
}
