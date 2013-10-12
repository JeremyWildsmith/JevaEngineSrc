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
