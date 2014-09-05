package io.github.jevaengine.rpgbase.netcommon.dialogue;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession;

public final class SubmitAnswer implements INetVisitor<DialogueSession>
{
	private String m_answer;
	
	//Used by kryo
	@SuppressWarnings("unused")
	private SubmitAnswer() { }
	
	public SubmitAnswer(String answer)
	{
		m_answer = answer;
	}
	
	@Override
	public void visit(Communicator sender, DialogueSession host, boolean onServer) throws InvalidMessageException 
	{
		host.listenerSay(m_answer);
	}

	@Override
	public Class<DialogueSession> getHostComponentClass()
	{
		return DialogueSession.class;
	}
}