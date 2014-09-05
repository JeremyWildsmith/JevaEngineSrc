package io.github.jevaengine.rpgbase;

import io.github.jevaengine.util.Nullable;

public interface ILoadout extends IImmutableLoadout
{
	public void clear();
	public Item unequip(ItemType type);
	
	@Nullable
	public Item equip(Item item);
	
}
