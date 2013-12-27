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
import io.github.jevaengine.IResourceLibrary;
import java.util.HashMap;

import io.github.jevaengine.game.Game;
import io.github.jevaengine.game.IGameScriptProvider;
import io.github.jevaengine.rpgbase.quest.QuestState;
import io.github.jevaengine.util.Nullable;

public abstract class RpgGame extends Game
{
	private DialogueController m_dialogueController = new DialogueController();
	
	@Override
	protected void startup()
	{
	}
	
	@Override
	public void update(int deltaTime)
	{
		super.update(deltaTime);
		m_dialogueController.update(deltaTime);
	}

	public final DialogueController getDialogueController()
	{
		return m_dialogueController;
	}
	
	@Override
	public IGameScriptProvider getScriptBridge()
	{
		return new RpgGameScriptProvider();
	}

	public abstract @Nullable RpgCharacter getPlayer();

	public class RpgGameScriptProvider implements IGameScriptProvider
	{
		@Override
		public Object getGameBridge()
		{
			return new GameBridge();
		}

		@Override
		public HashMap<String, Object> getGlobals()
		{
			HashMap<String, Object> vars = new HashMap<String, Object>();

			vars.put("quest_notStarted", QuestState.NotStarted);
			vars.put("quest_failed", QuestState.Failed);
			vars.put("quest_completed", QuestState.Completed);
			vars.put("quest_inProgress", QuestState.InProgress);

			return vars;
		}

		public class GameBridge
		{
			public RpgCharacter.EntityBridge<?> getPlayer()
			{
				RpgCharacter character = RpgGame.this.getPlayer();

				return character == null ? null : character.getScriptBridge();
			}
			
			public void initiateDialogue(RpgCharacter.EntityBridge<?> speaker, String dialoguePath, int entry)
			{
				DialoguePath path = new DialoguePath();
				path.deserialize(Core.getService(IResourceLibrary.class).openConfiguration(dialoguePath));
				
				m_dialogueController.enqueueDialogue(speaker.getEntity(), path, entry);
			}
		}
	}
}
