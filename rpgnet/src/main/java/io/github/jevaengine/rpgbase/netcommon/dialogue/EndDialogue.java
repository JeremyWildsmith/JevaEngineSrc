package io.github.jevaengine.rpgbase.netcommon.dialogue;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession;

public class EndDialogue implements INetVisitor<DialogueSession>
{
	@Override
	public void visit(Communicator sender, DialogueSession host, boolean onServer) throws InvalidMessageException
	{
		host.cancel();
	}
	
	@Override
	public Class<DialogueSession> getHostComponentClass()
	{
		return DialogueSession.class;
	}
}
