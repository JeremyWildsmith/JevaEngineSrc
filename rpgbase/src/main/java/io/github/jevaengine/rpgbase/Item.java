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

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.ScriptDelegate;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.script.ScriptExecuteException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Item
{
	private ItemIdentifier m_identifer;

	private String m_name;
	private String m_description;

	private ItemType m_type;
	private Sprite m_graphic;

	private ItemBridge m_bridge;

	private final Logger m_logger = LoggerFactory.getLogger(Item.class);
	
	public Item(IScriptFactory scriptFactory, IAudioClipFactory audioClipFactory, String script, ItemIdentifier identifer, String name, Sprite graphic, ItemType itemType, String description)
	{
		m_bridge = new ItemBridge(audioClipFactory, scriptFactory.getFunctionFactory());
		
		m_identifer = identifer;
		m_name = name;
		m_graphic = graphic;
		m_type = itemType;
		m_description = description;
		
		try
		{
			scriptFactory.create(m_bridge, script);
		} catch(AssetConstructionException e)
		{
			m_logger.error("Error instantiating script for item " + m_name, e);
		}
	}

	public String[] getCommands()
	{
		try
		{
			return m_bridge.getCommands.hasHandler() ? m_bridge.getCommands.fire() : new String[0];		
		}catch(ScriptExecuteException e)
		{
			m_logger.error("getCommands handler failed on item " + m_name, e);
			return new String[0];
		}
	}

	public void doCommand(RpgCharacter user, DefaultItemSlot slot, String command)
	{
		try
		{
			m_bridge.onCommand.fire(user, slot, command);
		} catch (ScriptExecuteException e)
		{
			m_logger.error("doCommand delegate failed on item " + m_name, e);
		}
	}
	
	public ItemType getType()
	{
		return m_type;
	}

	public ItemIdentifier getDescriptor()
	{
		return m_identifer;
	}

	public String getName()
	{
		return m_name;
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
		try
		{
			return m_bridge.doUse.hasHandler() ? m_bridge.doUse.fire(user.getBridge(), target.getBridge()) : false;
		} catch(ScriptExecuteException e)
		{
			m_logger.error("doUse handler failed on item " + m_name, e);
			return false;
		}
	}

	public static class ItemBridge
	{
		private final IAudioClipFactory m_audioClipFactory;
		
		private final Logger m_logger = LoggerFactory.getLogger(ItemBridge.class);
		
		public final ScriptDelegate<String[]> getCommands;
		public final ScriptDelegate<Boolean> doUse;
		public final ScriptEvent onCommand;
		
		public ItemBridge(IAudioClipFactory audioClipFactory, IFunctionFactory functionFactory)
		{
			m_audioClipFactory = audioClipFactory;
			getCommands = new ScriptDelegate<>(functionFactory, String[].class);
			doUse = new ScriptDelegate<>(functionFactory, Boolean.class);
			onCommand = new ScriptEvent(functionFactory);
		}
		
		public void playAudio(String audioName)
		{
			try
			{
				m_audioClipFactory.create(audioName).play();
			} catch (AssetConstructionException e)
			{
				m_logger.error("Error occured attempting to play audio", e);
			}
		}
	}
}
