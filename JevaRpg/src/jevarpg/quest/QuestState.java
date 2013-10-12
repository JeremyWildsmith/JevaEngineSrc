package jevarpg.quest;

import proguard.annotation.KeepPublicClassMemberNames;

@KeepPublicClassMemberNames
public enum QuestState
{
    NotStarted("Not Started"), InProgress("In Progress"), Completed("Completed"), Failed("Failed");

    private String m_asString;

    private QuestState(String asString)
    {
        m_asString = asString;
    }

    @Override
    public String toString()
    {
        return m_asString;
    }
}
