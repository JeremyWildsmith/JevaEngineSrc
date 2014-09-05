package io.github.jevaengine.rpgbase;

import io.github.jevaengine.util.Nullable;

public interface IImmutableItemStore
{
	void removeObserver(IItemStoreObserver observer);
	void addObserver(IItemStoreObserver observer);
	
	boolean isFull();
	
	IImmutableItemSlot[] getSlots();
	
	@Nullable
	IImmutableItemSlot getEmptySlot();
}
