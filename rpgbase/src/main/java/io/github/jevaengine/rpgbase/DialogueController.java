/**
 * *****************************************************************************
 * Copyright (c) 2013 Jeremy. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the GNU Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
* If you'd like to obtain a another license to this code, you may contact
 * Jeremy to discuss alternative redistribution options.
 * 
* Contributors: Jeremy - initial API and implementation
*****************************************************************************
 */
package io.github.jevaengine.rpgbase;

import io.github.jevaengine.rpgbase.DialoguePath.Answer;
import io.github.jevaengine.rpgbase.DialoguePath.Query;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Entity;
import java.util.LinkedList;
import java.util.Queue;

public final class DialogueController
{
	private Queue<DialoguePathEntry> m_dialogueQueue = new LinkedList<DialoguePathEntry>();
	
	private DialoguePathEntry m_currentDialogue;
	
	private boolean m_isBusy = false;
	
	private Observers m_observers = new Observers();
	
	protected DialogueController()
	{
		
	}

	private void beginDialogue(DialoguePathEntry entry)
	{
		m_isBusy = true;
		m_currentDialogue = entry;
		inquire(entry.path.queries[entry.initialQuery]);
		m_observers.beginDialogue();
	}
	
	private void endDialogue()
	{
		m_isBusy = false;
		m_currentDialogue = null;
		m_observers.endDialogue();
	}
	
	private void inquire(Query query)
	{
		m_currentDialogue.currentQuery = query;
		m_observers.speakerInquired(query.query);
	}
	
	public void addObserver(IDialogueControlObserver observer)
	{
		m_observers.add(observer);
	}
	
	public void removeObserver(IDialogueControlObserver observer)
	{
		m_observers.remove(observer);
	}
	
	public void enqueueDialogue(@Nullable Entity speaker, DialoguePath path, int entry)
	{
		m_dialogueQueue.add(new DialoguePathEntry(speaker, path, entry));
	}
	
	public boolean say(String message)
	{
		m_observers.listenerSaid(message);
		
		if(!m_isBusy)
			return false;
	
		Answer answer = null;
			
		for(Answer a : m_currentDialogue.currentQuery.answers)
		{
			if(a.answer.equals(message))
				answer = a;
		}
		
		if(answer == null)
			return false;
		
		int next = answer.next;
					
		if(answer.event >= 0)
			m_observers.dialogueEvent(answer.event);
		
		if(next >= 0)
			inquire(m_currentDialogue.path.queries[next]);
		else
			endDialogue();
		
		return true;
	}
	
	void update(int deltaTime)
	{
		if(!m_isBusy && !m_dialogueQueue.isEmpty())
			beginDialogue(m_dialogueQueue.remove());
	}
	
	public boolean isBusy()
	{
		return m_isBusy;
	}

	public @Nullable Entity getSpeaker()
	{
		return m_currentDialogue == null ? null : m_currentDialogue.speaker;
	}
	
	public String[] getAnswers()
	{
		if(!m_isBusy)
			return new String[] {};
		
		Answer[] answers = m_currentDialogue.currentQuery.answers;
		String[] strAnswers = new String[answers.length];
		
		for(int i = 0; i < answers.length; i++)
			strAnswers[i] = answers[i].answer;
		
		return strAnswers;
	}

	public void cancelCurrent()
	{
		if(m_isBusy)
			endDialogue();
	}
	
	public void clear()
	{
		cancelCurrent();
		m_dialogueQueue.clear();
	}
	
	public interface IDialogueControlObserver
	{
		void beginDialogue();
		void endDialogue();
		void dialogueEvent(int event);
		void speakerSaid(String message);
		void listenerSaid(String message);
	}
	
	private class Observers extends StaticSet<IDialogueControlObserver>
	{
		
		void beginDialogue()
		{
			for(IDialogueControlObserver o : this)
				o.beginDialogue();
		}
		
		void endDialogue()
		{
			for(IDialogueControlObserver o : this)
				o.endDialogue();
		}
				
		void dialogueEvent(int event)
		{
			for(IDialogueControlObserver o : this)
				o.dialogueEvent(event);
		}
		
		void speakerInquired(String message)
		{
			for(IDialogueControlObserver o : this)
				o.speakerSaid(message);
		}
		
		void listenerSaid(String message)
		{
			for(IDialogueControlObserver o : this)
				o.listenerSaid(message);
		}
	}
	
	private class DialoguePathEntry
	{
		public @Nullable Entity speaker;
		public DialoguePath path;
		public Query currentQuery;
		public int initialQuery;
		
		public DialoguePathEntry(Entity _speaker, DialoguePath _path, int _initialQuery)
		{
			speaker = _speaker;
			path = _path;
			initialQuery = _initialQuery;
		}
	}
}
