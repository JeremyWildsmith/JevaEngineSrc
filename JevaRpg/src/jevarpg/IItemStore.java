/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
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
