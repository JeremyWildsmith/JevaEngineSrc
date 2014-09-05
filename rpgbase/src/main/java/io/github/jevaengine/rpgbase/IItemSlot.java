package io.github.jevaengine.rpgbase;

import io.github.jevaengine.util.Nullable;

public interface IItemSlot extends IImmutableItemSlot
{
	@Nullable
	Item setItem(Item item);

	@Nullable
	Item clear();
}
