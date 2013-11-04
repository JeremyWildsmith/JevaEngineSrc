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

import java.util.HashMap;

import io.github.jevaengine.Core;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.config.VariableStore;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.game.IGameScriptProvider;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.ui.UIStyle;
import io.github.jevaengine.rpgbase.library.RpgEntityLibrary;
import io.github.jevaengine.rpgbase.quest.QuestState;
import io.github.jevaengine.util.Nullable;

public abstract class RpgGame extends Game
{
	private UIStyle m_gameStyle;

	private Sprite m_cursor;

	@Override
	protected void startup()
	{
		IResourceLibrary fileSystem = Core.getService(IResourceLibrary.class);

		m_gameStyle = UIStyle.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/small.juis")));

		m_cursor = Sprite.create(VariableStore.create(fileSystem.openResourceStream("ui/tech/cursor.jsf")));
		m_cursor.setAnimation("idle", AnimationState.Play);

	}

	public RpgEntityLibrary getEntityLibrary()
	{
		return new RpgEntityLibrary();
	}

	@Override
	public void update(int deltaTime)
	{
		m_cursor.update(deltaTime);

		super.update(deltaTime);
	}

	@Override
	public UIStyle getGameStyle()
	{
		return m_gameStyle;
	}

	@Override
	protected Sprite getCursor()
	{
		return m_cursor;
	}

	@Override
	public IGameScriptProvider getScriptBridge()
	{
		return new RpgGameScriptProvider();
	}

	public abstract @Nullable
	RpgCharacter getPlayer();

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
				if (RpgGame.this.getPlayer() == null)
					return null;

				return RpgGame.this.getPlayer().getScriptBridge();
			}
		}
	}
}
