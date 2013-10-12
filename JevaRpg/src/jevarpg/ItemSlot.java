package jevarpg;

import java.util.NoSuchElementException;

public class ItemSlot
{
    private Item m_item;

    public ItemSlot()
    {
        m_item = null;
    }

    public ItemSlot(Item item)
    {
        m_item = item;
    }

    public boolean isEmpty()
    {
        return m_item == null;
    }

    public Item getItem()
    {
        if (m_item == null)
            throw new NoSuchElementException();

        return m_item;
    }

    public void setItem(Item item)
    {
        m_item = item;
    }

    public void clear()
    {
        m_item = null;
    }
}
