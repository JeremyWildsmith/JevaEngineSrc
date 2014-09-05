package io.github.jevaengine.rpgbase;

import io.github.jevaengine.rpgbase.character.RpgCharacter;

public interface IItemStoreObserver
{
	void addItem(int slotIndex, Item item);
	void removeItem(int slotIndex, Item item);
	void itemAction(int slotIndex, RpgCharacter accessor, String action);
}