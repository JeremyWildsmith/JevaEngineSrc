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

import io.github.jevaengine.util.Nullable;

import java.util.NoSuchElementException;

public final class DefaultItemSlot implements IItemSlot
{
	@Nullable
	private Item m_item;

	public DefaultItemSlot() { }
	
	public DefaultItemSlot(Item item)
	{
		m_item = item;
	}

	@Override
	public boolean isEmpty()
	{
		return m_item == null;
	}

	@Override
	@Nullable
	public Item getItem()
	{
		if (m_item == null)
			throw new NoSuchElementException();

		return m_item;
	}

	@Override
	public Item setItem(Item item)
	{
		Item prev = m_item;
		m_item = item;
		return prev;	
	}

	@Override
	@Nullable
	public Item clear()
	{
		Item prev = m_item;
		m_item = null;
		return prev;
	}
}
