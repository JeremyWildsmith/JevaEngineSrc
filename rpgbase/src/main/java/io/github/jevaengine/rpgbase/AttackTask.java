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
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.ITask;
import io.github.jevaengine.world.WorldDirection;

public class AttackTask implements ITask
{
	private static final int ATTACK_TICK_INTERVAL = 75;

	private RpgCharacter m_attackee;
	private IAttacker m_attacker;

	private WorldDirection m_lastWorldDirection;

	private int m_attackTimeout;

	private boolean m_queryCancel;

	public AttackTask(IAttacker attacker, @Nullable RpgCharacter attackee)
	{
		m_attacker = attacker;
		m_attackee = attackee;
		m_attackTimeout = 0;
		m_lastWorldDirection = WorldDirection.Zero;

		m_queryCancel = false;
	}

	@Override
	public void cancel()
	{
		m_queryCancel = true;
	}

	@Override
	public void begin(Entity entity)
	{
		m_queryCancel = false;
	}

	@Override
	public void end()
	{
	}

	public void setTarget(RpgCharacter attackee)
	{
		m_attackee = attackee;
	}

	@Override
	public boolean doCycle(int deltaTime)
	{
		if (m_queryCancel ||
				m_attackee == null ||
				m_attacker.getCharacter().isDead() ||
				m_attackee.isDead() ||
				!m_attacker.getCharacter().isAssociated() ||
				!m_attackee.isAssociated() ||
				m_attackee.getWorld() != m_attacker.getCharacter().getWorld())
		{
			return true;
		}

		m_attackTimeout += deltaTime;

		for (; m_attackTimeout >= ATTACK_TICK_INTERVAL; m_attackTimeout -= ATTACK_TICK_INTERVAL)
		{
			WorldDirection attackDirection = WorldDirection.fromVector(m_attackee.getLocation().difference(m_attacker.getCharacter().getLocation()));

			if (m_attacker.attack(m_attackee))
			{
				if (m_lastWorldDirection != attackDirection)
					m_attacker.getCharacter().setDirection(attackDirection);

				m_lastWorldDirection = attackDirection;
			} else
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isParallel()
	{
		return false;
	}

	@Override
	public boolean ignoresPause()
	{
		// TODO Auto-generated method stub
		return false;
	}

	public interface IAttacker
	{
		boolean attack(RpgCharacter target);

		RpgCharacter getCharacter();
	}

}
