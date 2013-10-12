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
import jeva.game.Game;
import jeva.graphics.ui.IDialogResponder;

/**
 * The Class DialogTask.
 */
public abstract class DialogTask implements ITask
{

	/** The m_is busy. */
	private boolean m_isBusy = true;

	/** The m_subject. */
	private Entity m_subject;

	/**
	 * Instantiates a new dialog task.
	 * 
	 * @param subject
	 *            the subject
	 */
	public DialogTask(@Nullable Entity subject)
	{
		m_subject = subject;
	}

	/**
	 * Instantiates a new dialog task.
	 */
	public DialogTask()
	{
		this(null);
	}

	/**
	 * Display query.
	 * 
	 * @param entry
	 *            the entry
	 */
	private void displayQuery(final Query entry)
	{
		final HashMap<String, Answer> answers = new HashMap<String, Answer>();

		for (Answer a : entry.getAnswers())
			answers.put(a.getAnswer(), a);

		Core.getService(Game.class).getDialog().issueQuery(entry.getQuery(), answers.keySet().toArray(new String[answers.keySet().size()]), new IDialogResponder()
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
		displayQuery(this.getEntryDialog());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#end()
	 */
	@Override
	public void end()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
	}

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

	/**
	 * On dialog end.
	 */
	public abstract void onDialogEnd();

	/**
	 * On event.
	 * 
	 * @param subject
	 *            the subject
	 * @param eventCode
	 *            the event code
	 * @return the query
	 */
	public abstract Query onEvent(@Nullable Entity subject, int eventCode);

	/**
	 * Gets the entry dialog.
	 * 
	 * @return the entry dialog
	 */
	public abstract Query getEntryDialog();
}
