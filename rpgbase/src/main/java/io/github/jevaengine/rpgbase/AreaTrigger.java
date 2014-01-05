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
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.EffectMap;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.RectangleSearchFilter;

public class AreaTrigger extends Entity
{
	private static final int SCAN_INTERVAL = 400;

	private int m_lastScan;

	private TriggerScript m_script = new TriggerScript();

	private ArrayList<RpgCharacter> m_includedEntities = new ArrayList<RpgCharacter>();

	private float m_width;
	private float m_height;
	
	public AreaTrigger(@Nullable String name, IVariable arguments)
	{
		super(name, arguments, new AreaTriggerBridge<>());

		AreaTriggerDeclaration decl = arguments.getValue(AreaTriggerDeclaration.class);
	
		m_width = decl.width;
		m_height = decl.height;
	
	}

	private Rect2F getContainingBounds()
	{
		Vector2F location = getLocation();
		
		//The bounds for an area trigger are meant to be interpreted as containing
		//whole tiles. I.e, a width of 1, and height of 1, located at 0,0 should contain the tile 0,0.
		//The way the engine interprets a rect located at 0,0 with a width 1 and height 1 is to have it start
		//from the origin of 0,0 (the center of the tile at 0,0). In this case, we want it to start
		//from the corner, thus containing the entire tile...

		return new Rect2F(location.x - 0.5F, location.y - 0.5F, m_width, m_height);
	}
	
	@Override
	public void doLogic(int deltaTime)
	{
		m_lastScan -= deltaTime;

		if (m_lastScan <= 0)
		{
			m_lastScan = SCAN_INTERVAL;

			Actor[] entities = getWorld().getActors(new RectangleSearchFilter<Actor>(getContainingBounds()));

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

		@Override
		public void flagSet(String name, int value) { }

		@Override
		public void flagCleared(String name) { }
	}
	
	public static class AreaTriggerDeclaration implements ISerializable
	{
		public float width;
		public float height;
		
		public AreaTriggerDeclaration() { }
		
		@Override
		public void serialize(IVariable target)
		{
			target.addChild("width").setValue(this.width);
			target.addChild("height").setValue(this.height);
		}

		@Override
		public void deserialize(IVariable source)
		{
			this.width = source.getChild("width").getValue(Integer.class);
			this.height = source.getChild("height").getValue(Integer.class);
		}
	}
}
