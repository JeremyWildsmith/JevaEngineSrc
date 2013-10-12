package jevarpg;

import jevarpg.Item.ItemDescriptor;

public interface IItemStore
{

    boolean allowStoreAccess(RpgCharacter accessor);

    public ItemSlot[] getSlots();

    int getSlotIndex(ItemSlot slot);

    boolean hasItem(ItemDescriptor item);

    boolean addItem(ItemDescriptor item);

    boolean removeItem(ItemDescriptor item);

    void doSlotAction(RpgCharacter accessor, String action, int slotIndex);

    public String[] getSlotActions(RpgCharacter accessor, int slotIndex);

}
