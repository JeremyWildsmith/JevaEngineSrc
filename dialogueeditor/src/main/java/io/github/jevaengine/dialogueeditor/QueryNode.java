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

import io.github.jevaengine.game.DialogPath.Query;

import java.util.ArrayList;

public class QueryNode extends Node
{
	private ArrayList<AnswerNode> m_answers = new ArrayList<AnswerNode>();
	private int m_id;

	public QueryNode()
	{
		super("Default", -1);
		m_id = -1;
	}

	public int getId()
	{
		return m_id;
	}

	public void setId(int id)
	{
		m_id = id;
	}

	@Override
	public String toString()
	{
		return "Q: (" + (m_id >= 0 ? String.valueOf(m_id) : "?") + ") " + super.toString();
	}

	public void addAnswer(AnswerNode answer)
	{
		m_answers.add(answer);
	}

	public void removeAnswer(AnswerNode answer)
	{
		m_answers.remove(answer);
	}

	public AnswerNode[] getAnswers()
	{
		return m_answers.toArray(new AnswerNode[m_answers.size()]);
	}

	public Query toJevaQuery()
	{
		Query q = new Query(m_id, getText(), this.getEventCode());

		for (AnswerNode node : m_answers)
			q.addAnswer(node.toJevaAnswer());

		return q;
	}
}
