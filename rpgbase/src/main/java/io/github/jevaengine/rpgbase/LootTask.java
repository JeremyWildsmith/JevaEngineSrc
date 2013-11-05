package io.github.jevaengine.rpgbase;

import java.lang.ref.WeakReference;

import io.github.jevaengine.Core;
import io.github.jevaengine.graphics.ui.IWindowManager;
import io.github.jevaengine.rpgbase.ui.InventoryMenu;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.ITask;

public class LootTask implements ITask
{
	private InventoryMenu m_inventoryMenu = new InventoryMenu();

	private boolean m_isQueryEnd = false;

	private WeakReference<IItemStore> m_target;
	private RpgCharacter m_looter;

	public LootTask(RpgCharacter looter, IItemStore target)
	{
		m_looter = looter;
		m_target = new WeakReference<IItemStore>(target);
	}

	@Override
	public void cancel()
	{
		m_isQueryEnd = true;
	}

	@Override
	public void begin(Entity entity)
	{
		Core.getService(IWindowManager.class).addWindow(m_inventoryMenu);
			
		if (m_target.get() == null || m_looter != entity)
			cancel();
		else
			m_inventoryMenu.accessInventory(m_target.get(), m_looter);
	}

	@Override
	public void end()
	{
		Core.getService(IWindowManager.class).removeWindow(m_inventoryMenu);
	}

	@Override
	public boolean doCycle(int deltaTime)
	{
		if (m_isQueryEnd)
			return true;

		return !m_inventoryMenu.isVisible();
	}

	@Override
	public boolean isParallel()
	{
		return false;
	}

	@Override
	public boolean ignoresPause()
	{
		return true;
	}

}
