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
package jevarpg.quest;

import proguard.annotation.KeepPublicClassMemberNames;

@KeepPublicClassMemberNames
public class QuestTask
{
    private QuestState m_state;

    private String m_id;
    private String m_name;
    private String m_description;

    public QuestTask(String id, String name, String description)
    {
        m_state = QuestState.NotStarted;

        m_id = id;
        m_name = name;
        m_description = description;
    }

    public String getId()
    {
        return m_id;
    }

    public String getName()
    {
        return m_name;
    }

    public String getDescription()
    {
        return m_description;
    }

    public QuestState getState()
    {
        return m_state;
    }

    public void setState(QuestState state)
    {
        m_state = state;
    }
}
