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
package io.github.jevaengine.dialogueeditor;

import io.github.jevaengine.game.DialogPath.Answer;


public class AnswerNode extends Node
{
	private QueryNode m_nextDialog;

	public AnswerNode()
	{
		super("Default", -1);

		m_nextDialog = null;
	}

	public QueryNode getDialog()
	{
		return m_nextDialog;
	}

	public void setDialog(QueryNode next)
	{
		m_nextDialog = next;
	}

	@Override
	public String toString()
	{
		return "A: " + super.toString();
	}

	public Answer toJevaAnswer()
	{
		return new Answer(getText(), getEventCode(), (m_nextDialog == null ? null : m_nextDialog.toJevaQuery()));
	}
}
