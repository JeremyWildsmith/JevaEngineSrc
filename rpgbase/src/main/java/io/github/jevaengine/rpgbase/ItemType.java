package io.github.jevaengine.rpgbase;

public enum ItemType
{
	General(),
	Weapon(true),
	HeadArmor(true),
	BodyArmor(true),
	Accessory(true);
	
	private boolean m_isWieldable;

	ItemType()
	{
		m_isWieldable = false;
	}

	ItemType(boolean isWieldable)
	{
		m_isWieldable = isWieldable;
	}
	
	public boolean isWieldable()
	{
		return m_isWieldable;
	}
}