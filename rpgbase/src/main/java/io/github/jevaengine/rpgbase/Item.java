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

import io.github.jevaengine.Core;
import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.Script;
import io.github.jevaengine.audio.Audio;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.util.Nullable;
import javax.script.ScriptException;
import org.mozilla.javascript.NativeArray;

public class Item
{
	private ItemIdentifer m_identifer;

	private String m_name;
	private String m_description;

	private Sprite m_graphic;

	private ItemType m_type;

	private Script m_script;

	public enum ItemType
	{
		General(new NoItemBehaviour()),
		Weapon(true, new AttackItemBehaviour(), "item/weaponGear.jsf"),
		HeadArmor(true, new NoItemBehaviour(), "item/headGear.jsf"),
		BodyArmor(true, new NoItemBehaviour(), "item/bodyGear.jsf"),
		Accessory(true, new NoItemBehaviour(), "item/accessoryGear.jsf");

		private Sprite m_icon;
		private IItemBehaviour m_behaviour;
		private boolean m_isWieldable;

		ItemType(IItemBehaviour behaviour)
		{
			m_behaviour = behaviour;
			m_icon = null;
			m_isWieldable = false;
		}

		ItemType(boolean isWieldable, IItemBehaviour behaviour, String backgroundSpritePath)
		{
			m_isWieldable = isWieldable;
			m_behaviour = behaviour;
			m_icon = Sprite.create(Core.getService(IResourceLibrary.class).openConfiguration(backgroundSpritePath));
			m_icon.setAnimation("idle", AnimationState.Play);
		}

		public IItemBehaviour getBehaviour()
		{
			return m_behaviour;
		}

		public boolean hasIcon()
		{
			return m_icon != null;
		}
		
		public boolean isWieldable()
		{
			return m_isWieldable;
		}

		@Nullable
		public Sprite getIcon()
		{
			return m_icon;
		}
	}

	public Item(ItemIdentifer identifer, String name, Sprite graphic, ItemType itemType, Script script, String description)
	{
		m_identifer = identifer;
		m_name = name;
		m_graphic = graphic;
		m_type = itemType;
		m_script = script;
		m_description = description;
	}

	public static Item create(ItemIdentifer identifier)
	{
		ItemDeclaration itemDecl = Core.getService(IResourceLibrary.class)
									.openConfiguration(identifier.m_descriptor)
									.getValue(ItemDeclaration.class);

		Sprite graphic = Sprite.create(
							Core.getService(IResourceLibrary.class)
								.openConfiguration(itemDecl.sprite));
		
		Script script;

		if (itemDecl.script != null)
			script = Core.getService(IResourceLibrary.class).openScript(itemDecl.script, new ItemBridge());
		else
			script = new Script();
		
		graphic.setAnimation("idle", AnimationState.Play);

		return new Item(identifier, itemDecl.name, graphic, itemDecl.type, script, itemDecl.description);

	}

	public static Item create(String descriptor)
	{
		return create(new ItemIdentifer(descriptor));
	}

	public String[] getCommands()
	{
		try
		{
			NativeArray jsStringArray = (NativeArray) getScript().invokeScriptFunction("getCommands");

			if (jsStringArray == null)
				return new String[0];

			String[] commands = new String[(int) jsStringArray.getLength()];

			for (int i = 0; i < commands.length; i++)
			{
				Object element = jsStringArray.get(i, null);

				if (!(element instanceof String))
					throw new CoreScriptException("Unexpected data returned on invoking getCommands for Actor Interactable entity.");

				commands[i] = (String) element;
			}
			
			return commands;
		} catch (NoSuchMethodException ex)
		{
			return new String[0];
		} catch (ScriptException ex)
		{
			throw new CoreScriptException(ex);
		}
	}

	public void doCommand(RpgCharacter user, ItemSlot slot, String command)
	{
		try
		{
			m_script.invokeScriptFunction("doCommand", user, slot.getScriptBridge(), command);
			
		} catch (NoSuchMethodException ex) { 
		} catch (ScriptException ex)
		{
			throw new CoreScriptException(ex);
		}
	}
	
	public ItemType getType()
	{
		return m_type;
	}

	public ItemIdentifer getDescriptor()
	{
		return m_identifer;
	}

	public String getName()
	{
		return m_name;
	}

	public Script getScript()
	{
		return m_script;
	}

	public IRenderable getGraphic()
	{
		return m_graphic;
	}

	public String getDescription()
	{
		return m_description;
	}

	public boolean use(RpgCharacter user, RpgCharacter target)
	{
		return m_type.getBehaviour().use(this, user, target);
	}

	public static class ItemIdentifer implements Comparable<ItemIdentifer>
	{
		private String m_descriptor;

		public ItemIdentifer(String descriptor)
		{
			m_descriptor = descriptor.trim().toLowerCase().replace('\\', '/');
			m_descriptor = (m_descriptor.startsWith("/") ? m_descriptor.substring(1) : m_descriptor);
		}

		@Override
		public int compareTo(ItemIdentifer item)
		{
			return m_descriptor.compareTo(item.m_descriptor);
		}

		@Override
		public boolean equals(Object o)
		{
			if (o instanceof ItemIdentifer)
				return ((ItemIdentifer) o).m_descriptor.compareTo(m_descriptor) == 0;
			else
				return new ItemIdentifer(o.toString()).m_descriptor.compareTo(m_descriptor) == 0;
		}

		@Override
		public String toString()
		{
			return m_descriptor;
		}
	}

	public static class ItemBridge
	{
		public void playAudio(String audioName)
		{
			new Audio(audioName).play();
		}
	}
	
	public static class ItemDeclaration implements ISerializable
	{
		public String name;
		public ItemType type;
		public String sprite;
		
		public String description;
		
		@Nullable
		public String script;

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("name").setValue(name);
			target.addChild("type").setValue(type.ordinal());
			target.addChild("sprite").setValue(sprite);
			
			if(description != null && description.length() > 0)
				target.addChild("description").setValue(description);
			
			if(script != null && script.length() > 0)
				target.addChild("script").setValue(script);
		}

		@Override
		public void deserialize(IVariable source)
		{
			name = source.getChild("name").getValue(String.class);
			type = ItemType.values()[source.getChild("type").getValue(Integer.class)];
			sprite = source.getChild("sprite").getValue(String.class);
			
			if(source.childExists("description"))
				description = source.getChild("description").getValue(String.class);
			
			if(source.childExists("script"))
				script = source.getChild("script").getValue(String.class);
		}
	}
}
