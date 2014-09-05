package io.github.jevaengine.rpgbase.netcommon.dialogue;

import io.github.jevaengine.netcommon.entity.NetEntityIdentifier;

public final class QueryAnswer
{
	private String m_query;
	private String[] m_answers;

	private NetEntityIdentifier m_speaker;
	private NetEntityIdentifier m_listener;
	
	//Used by kryo
	@SuppressWarnings("unused")
	private QueryAnswer() { }
	
	public QueryAnswer(NetEntityIdentifier speaker, NetEntityIdentifier listener, String query, String[] answers)
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
	
	public NetEntityIdentifier getSpeaker()
	{
		return m_speaker;
	}
	
	public NetEntityIdentifier getListener()
	{
		return m_listener;
	}
}