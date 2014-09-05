package io.github.jevaengine.rpgbase;


public class ItemIdentifier implements Comparable<ItemIdentifier>
{
	private String m_descriptor;

	public ItemIdentifier(String descriptor)
	{
		m_descriptor = descriptor.trim().toLowerCase().replace('\\', '/');
		m_descriptor = (m_descriptor.startsWith("/") ? m_descriptor.substring(1) : m_descriptor);
	}

	@Override
	public int compareTo(ItemIdentifier item)
	{
		return m_descriptor.compareTo(item.m_descriptor);
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof ItemIdentifier)
			return ((ItemIdentifier) o).m_descriptor.compareTo(m_descriptor) == 0;
		else
			return new ItemIdentifier(o.toString()).m_descriptor.compareTo(m_descriptor) == 0;
	}

	@Override
	public String toString()
	{
		return m_descriptor;
	}
}