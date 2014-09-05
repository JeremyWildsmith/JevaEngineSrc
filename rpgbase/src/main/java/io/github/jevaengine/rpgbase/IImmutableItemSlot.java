package io.github.jevaengine.rpgbase;

import io.github.jevaengine.util.Nullable;

public interface IImmutableItemSlot
{
	boolean isEmpty();
	
	@Nullable
	Item getItem();
}
