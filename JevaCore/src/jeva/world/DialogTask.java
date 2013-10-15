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
package jeva.world;

import java.util.HashMap;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.game.DialogPath;
import jeva.game.DialogPath.Answer;
import jeva.game.DialogPath.Query;
import jeva.graphics.ui.DialogMenu;
import jeva.graphics.ui.IDialogResponder;
import jeva.graphics.ui.IWindowManager;


public abstract class DialogTask implements ITask
{
	
	private boolean m_isBusy = true;
	private Entity m_subject;

	private DialogMenu m_dialogMenu = new DialogMenu();
	
	public DialogTask(@Nullable Entity subject)
	{
		m_subject = subject;
	}

	
	public DialogTask()
	{
		this(null);
	}

	
	private void displayQuery(final Query entry)
	{
		final HashMap<String, Answer> answers = new HashMap<String, Answer>();

		for (Answer a : entry.getAnswers())
			answers.put(a.getAnswer(), a);

		m_dialogMenu.issueQuery(entry.getQuery(), 
				answers.keySet().toArray(new String[answers.keySet().size()]), 
				new IDialogResponder()
		{

			@Override
			public void onAnswer(String answerText)
			{
				Answer answer = answers.get(answerText);

				Query next = null;

				if (answer.getEventCode() != DialogPath.EVENT_NONE)
					next = DialogTask.this.onEvent(m_subject, answer.getEventCode());
				else
					next = answer.getNextQuery();

				if (next != null)
				{
					if (next.getEventCode() != DialogPath.EVENT_NONE)
						DialogTask.this.onEvent(m_subject, next.getEventCode());

					displayQuery(next);
				} else
				{
					m_isBusy = false;
					onDialogEnd();
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{
		Core.getService(IWindowManager.class).addWindow(m_dialogMenu);
		displayQuery(this.getEntryDialog());
	}

	/*
	 * (non-Javadoc)
	 * @see jeva.world.ITask#end()
	 */
	@Override
	public void end()
	{
		Core.getService(IWindowManager.class).removeWindow(m_dialogMenu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel() { }

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	@Override
	public boolean doCycle(int deltaTime)
	{
		return !m_isBusy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#isParallel()
	 */
	@Override
	public boolean isParallel()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#ignoresPause()
	 */
	@Override
	public boolean ignoresPause()
	{
		return true;
	}

	public abstract void onDialogEnd();

	public abstract Query onEvent(@Nullable Entity subject, int eventCode);

	public abstract Query getEntryDialog();
}
