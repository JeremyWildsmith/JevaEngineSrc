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
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.InvalidTaskHostException;
import io.github.jevaengine.world.SynchronousOneShotTask;
import io.github.jevaengine.world.WorldDirection;

public abstract class AttackTask extends SynchronousOneShotTask
{
	private RpgCharacter m_attackee;
	
	private Observers m_observers = new Observers();
	
	public AttackTask(@Nullable RpgCharacter attackee)
	{
		m_attackee = attackee;
	}
	
	public void addObserver(IAttackObserver observer)
	{
		m_observers.add(observer);
	}
	
	public void removeObserver(IAttackObserver observer)
	{
		m_observers.remove(observer);
	}

	@Override
	public void run(Entity entity)
	{
		RpgCharacter attacker;
		
		if(entity instanceof RpgCharacter)
			attacker = (RpgCharacter)entity;
		else
			throw new InvalidTaskHostException("Task host must be an instanceof RpgCharacter.");
	
		if (m_attackee == null ||
			attacker.isDead() ||
			m_attackee.isDead() ||
			!attacker.isAssociated() ||
			!m_attackee.isAssociated() ||
			m_attackee.getWorld() != attacker.getWorld())
				return;
		
		if (doAttack(m_attackee))
			m_observers.attack(m_attackee);
	}

	public abstract boolean doAttack(RpgCharacter attackee);
	
	public interface IAttackObserver
	{
		void attack(RpgCharacter attackee);
	}
	
	private static class Observers extends StaticSet<IAttackObserver>
	{
		public void attack(RpgCharacter attackee)
		{
			for(IAttackObserver o : this)
				o.attack(attackee);
		}
	}
}
