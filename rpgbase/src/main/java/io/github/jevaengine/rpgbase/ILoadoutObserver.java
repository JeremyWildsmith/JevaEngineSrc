package io.github.jevaengine.rpgbase;


public interface ILoadoutObserver
{
	void unequip(ItemType gearType);
	void equip(Item item);
}