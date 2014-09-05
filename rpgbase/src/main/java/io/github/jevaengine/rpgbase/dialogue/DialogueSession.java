package io.github.jevaengine.rpgbase.dialogue;

import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.entity.IEntity;

public final class DialogueSession implements IDialogueListenerSession, IDialogueSpeakerSession
{
	private IDialogueSessionController m_dialogueSessionController;

	private SpeakerObservers m_speakerObservers = new SpeakerObservers();
	private ListenerObservers m_listenerObservers = new ListenerObservers();
	
	private DialogueQuery m_currentQuery;
	
	public DialogueSession(IDialogueSessionController route)
	{
		m_dialogueSessionController = route;
		setQuery(m_dialogueSessionController.getCurrentQuery());
	}
	
	private void setQuery(@Nullable DialogueQuery query)
	{
		if(query == m_currentQuery)
			return;
	
		m_currentQuery = query;

		if(m_currentQuery == null)
		{
			m_speakerObservers.end();
			m_listenerObservers.end();
		}else
			m_listenerObservers.speakerInquired(query);
	}
	
	@Override
	public void addObserver(IDialogueSpeakerSessionObserver observer)
	{
		m_speakerObservers.add(observer);
	}

	@Override
	public void addObserver(IDialogueListenerSessionObserver observer)
	{
		m_listenerObservers.add(observer);
	}
	
	public void removeObserver(IDialogueSpeakerSessionObserver observer)
	{
		m_speakerObservers.remove(observer);
	}

	@Override
	public void removeObserver(IDialogueListenerSessionObserver observer)
	{
		m_listenerObservers.remove(observer);
	}

	@Override
	public void listenerSay(String answer)
	{
		if(m_dialogueSessionController.parseAnswer(answer))
		{
			m_speakerObservers.listenerSaid(answer);
			setQuery(m_dialogueSessionController.getCurrentQuery());	
		}
	}

	@Override
	public boolean isActive()
	{
		return m_currentQuery != null;
	}

	@Override
	public void cancel()
	{
		m_dialogueSessionController.end();
		setQuery(m_dialogueSessionController.getCurrentQuery());
	}

	@Override
	@Nullable
	public DialogueQuery getCurrentQuery()
	{
		return m_currentQuery;
	}
	
	public interface IDialogueSessionController
	{
		DialogueQuery getCurrentQuery();
		boolean parseAnswer(String answer);
		
		void end();
	}

	private class SpeakerObservers extends StaticSet<IDialogueSpeakerSessionObserver>
	{	
		public void listenerSaid(String message)
		{
			for(IDialogueSpeakerSessionObserver o : this)
				o.listenerSaid(message);
		}
		
		public void end()
		{
			for(IDialogueSpeakerSessionObserver o : this)
				o.end();
		}
	}
	
	private class ListenerObservers extends StaticSet<IDialogueListenerSessionObserver>
	{
		public void speakerInquired(DialogueQuery query)
		{
			for(IDialogueListenerSessionObserver o : this)
				o.speakerInquired(query);
		}
		
		public void end()
		{
			for(IDialogueListenerSessionObserver o : this)
				o.end();
		}
	}
	
	public static final class DialogueQuery
	{
		private String m_query;
		private String[] m_answers;
		
		private IEntity m_speaker;
		private IEntity m_listener;
		
		public DialogueQuery(IEntity speaker, IEntity listener, String query, String[] answers)
		{
			m_speaker = speaker;
			m_listener = listener;
			
			m_query = query;
			m_answers = answers;
		}
		
		public String getQuery()
		{
			return m_query;
		}
		
		public String[] getAnswers()
		{
			return m_answers;
		}
		
		public IEntity getListener()
		{
			return m_listener;
		}
		
		public IEntity getSpeaker()
		{
			return m_speaker;
		}
	}
}
