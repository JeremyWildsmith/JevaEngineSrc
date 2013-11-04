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
package io.github.jevaengine.rpgbase;

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
