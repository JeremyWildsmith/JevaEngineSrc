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
package io.github.jevaengine.rpgbase.character;

import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.tasks.ITask;
import io.github.jevaengine.world.entity.tasks.InvalidTaskHostException;

public final class AttackTask implements ITask
{
	private RpgCharacter m_attackee;
	
	private IAttackHandler m_attackHandler;
	
	private final int m_attackPeriod;
	
	private int m_elapsedTime = 0;
	
	public AttackTask(@Nullable RpgCharacter attackee, int attackPeriod, IAttackHandler handler)
	{
		m_attackee = attackee;
		m_attackPeriod = attackPeriod;
		m_attackHandler = handler;
	}

	public interface IAttackHandler
	{
		void doAttack(RpgCharacter attackee);
	}

	@Override
	public void begin(IEntity entity)
	{
		if(!(entity instanceof RpgCharacter))
			throw new InvalidTaskHostException("Task host must be an instanceof RpgCharacter.");
	
		RpgCharacter attacker = (RpgCharacter)entity;

		if (m_attackee == null ||
			attacker.isDead() ||
			m_attackee.isDead() ||
			m_attackee.getWorld() != attacker.getWorld() ||
			m_attackee.getWorld() == null)
		{
				m_elapsedTime = m_attackPeriod;
		} else
		{
			m_elapsedTime = 0;
			m_attackHandler.doAttack(m_attackee);	
		}
	}

	@Override
	public void end() { }

	@Override
	public void cancel()
	{
		m_elapsedTime = m_attackPeriod;
	}

	@Override
	public boolean doCycle(int deltaTime)
	{
		m_elapsedTime += deltaTime;
		
		return m_elapsedTime >= m_attackPeriod;
	}

	@Override
	public boolean isParallel()
	{
		return false;
	}
}
