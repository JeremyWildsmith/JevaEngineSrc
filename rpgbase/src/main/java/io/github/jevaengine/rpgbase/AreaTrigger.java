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

import java.util.ArrayList;

import javax.script.ScriptException;

import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.EffectMap;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.RectangleSearchFilter;

public class AreaTrigger extends Entity
{
	private static final int SCAN_INTERVAL = 400;

	private Rect2F m_bounds;

	private int m_lastScan;

	private TriggerScript m_script = new TriggerScript();

	private ArrayList<RpgCharacter> m_includedEntities = new ArrayList<RpgCharacter>();

	public AreaTrigger(@Nullable String name, IVariable arguments)
	{
		super(name, arguments, new AreaTriggerBridge<>());

		m_bounds = arguments.getValue(AreaTriggerDeclaration.class).region;
	}

	@Override
	public void doLogic(int deltaTime)
	{
		m_lastScan -= deltaTime;

		if (m_lastScan <= 0)
		{
			m_lastScan = SCAN_INTERVAL;

			Actor[] entities = getWorld().getActors(new RectangleSearchFilter<Actor>(m_bounds));

			ArrayList<RpgCharacter> unfoundCharacters = new ArrayList<RpgCharacter>(m_includedEntities);

			for (Actor actor : entities)
			{
				if (!(actor instanceof RpgCharacter))
					continue;

				RpgCharacter character = (RpgCharacter) actor;

				if (!unfoundCharacters.contains(character))
				{
					m_includedEntities.add(character);
					m_script.onTrigger(true, character);
					character.addObserver(new TriggerCharacterObserver(character));
				} else
				{
					unfoundCharacters.remove(character);
				}
			}

			for (RpgCharacter character : unfoundCharacters)
			{
				m_includedEntities.remove(character);
				m_script.onTrigger(false, character);
			}
		}
	}

	@Override
	public void blendEffectMap(EffectMap globalEffectMap)
	{
	}

	public static class AreaTriggerBridge<T extends AreaTrigger> extends EntityBridge<T>
	{

	}

	private class TriggerScript
	{
		private void onTrigger(boolean isOver, RpgCharacter character)
		{
			try
			{
				getScript().invokeScriptFunction("onTrigger", character.getScriptBridge(), isOver);
			} catch (NoSuchMethodException e)
			{
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error invoking script routine onEnterTrigger " + e.getMessage());
			}
		}
	}

	private class TriggerCharacterObserver implements IEntityObserver
	{
		private RpgCharacter m_observee;

		public TriggerCharacterObserver(RpgCharacter observee)
		{
			m_observee = observee;
		}

		@Override
		public void leaveWorld()
		{
			m_includedEntities.remove(m_observee);
			m_observee.removeObserver(this);
		}

		@Override
		public void enterWorld()
		{
		}

		@Override
		public void replaced() { }
	}
	
	public static class AreaTriggerDeclaration implements ISerializable
	{
		public Rect2F region;
		
		public AreaTriggerDeclaration() { }
		
		@Override
		public void serialize(IVariable target)
		{
			target.addChild("region").setValue(this.region);
		}

		@Override
		public void deserialize(IVariable source)
		{
			this.region = source.getChild("region").getValue(Rect2F.class);
		}
	}
}
