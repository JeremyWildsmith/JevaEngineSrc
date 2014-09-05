package io.github.jevaengine.rpgbase;

import io.github.jevaengine.util.Nullable;

public interface IImmutableLoadout
{
	void addObserver(ILoadoutObserver observer);
	void removeObserver(ILoadoutObserver observer);
	
	@Nullable
	public DefaultItemSlot getSlot(ItemType gearType);
}
