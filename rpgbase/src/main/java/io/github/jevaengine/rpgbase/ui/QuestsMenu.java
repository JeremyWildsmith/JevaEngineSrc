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
package io.github.jevaengine.rpgbase.ui;

import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.rpgbase.quest.Quest;
import io.github.jevaengine.rpgbase.quest.QuestTask;

import java.awt.Color;

public class QuestsMenu extends Window
{
	private TextArea m_name;
	private TextArea m_description;
	private TextArea m_tasks;
	private TextArea m_state;

	private int m_currentQuest;
	private Quest[] m_quests;

	public QuestsMenu(UIStyle style)
	{
		super(style, 500, 600);

		this.setVisible(false);

		m_currentQuest = 0;
		m_quests = new Quest[0];

		m_name = new TextArea("None", Color.orange, 450, 70);
		m_name.setRenderBackground(false);

		m_description = new TextArea("", Color.orange, 450, 140);
		m_tasks = new TextArea("", Color.orange, 450, 140);
		m_state = new TextArea("", Color.orange, 350, 40);
		m_state.setRenderBackground(false);

		this.addControl(new Label("Quest Log", Color.red), new Vector2D(175, 10));
		this.addControl(m_state, new Vector2D(165, 70));
		this.addControl(new Label("Name:", Color.white), new Vector2D(20, 90));
		this.addControl(m_name, new Vector2D(20, 120));
		this.addControl(new Label("Description:", Color.white), new Vector2D(20, 180));
		this.addControl(m_description, new Vector2D(20, 210));
		this.addControl(new Label("Task:", Color.white), new Vector2D(20, 360));
		this.addControl(m_tasks, new Vector2D(20, 390));

		this.addControl(new Button("Next Quest")
		{
			@Override
			public void onButtonPress()
			{
				if (m_quests.length != 0)
				{
					m_currentQuest = (m_currentQuest + 1) % m_quests.length;
					showQuest(m_quests[m_currentQuest]);
				}

			}
		}, new Vector2D(165, 40));

		this.addControl(new Button("Close")
		{
			@Override
			public void onButtonPress()
			{
				QuestsMenu.this.setVisible(false);
			}
		}, new Vector2D(210, 560));
	}

	private void showQuest(Quest quest)
	{
		m_name.setText(quest.getName());
		m_description.setText(quest.getDescription());

		QuestTask[] tasks = quest.getTasks();

		StringBuilder tasksFormatted = new StringBuilder();

		for (QuestTask task : tasks)
		{
			tasksFormatted.append(String.format("------\nTask:%s\n\nDescription: %s\n\nStatus: %s\n\n", task.getName(), task.getDescription(), task.getState().toString()));
		}

		m_tasks.setText(tasks.toString());

		m_state.setText(quest.getState().toString());
	}

	public void showQuests(Quest[] quests)
	{
		this.setVisible(true);
		m_quests = quests;

		if (quests.length != 0)
			showQuest(quests[0]);
		else
		{
			m_name.setText("No Quests");
			m_description.setText("");
			m_tasks.setText("");
			m_state.setText("");
		}
	}

}
