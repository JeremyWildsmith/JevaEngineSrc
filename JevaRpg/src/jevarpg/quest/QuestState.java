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
