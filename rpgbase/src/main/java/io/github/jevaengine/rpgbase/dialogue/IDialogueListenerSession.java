package io.github.jevaengine.rpgbase.dialogue;

import io.github.jevaengine.rpgbase.dialogue.DialogueSession.DialogueQuery;
import io.github.jevaengine.util.Nullable;

public interface IDialogueListenerSession
{
	void listenerSay(String answer);
	void cancel();
	
	boolean isActive();
	
	@Nullable
	DialogueQuery getCurrentQuery();
	
	void addObserver(IDialogueListenerSessionObserver o);
	void removeObserver(IDialogueListenerSessionObserver o);
	
	public interface IDialogueListenerSessionObserver
	{
		void speakerInquired(DialogueQuery query);
		void end();
	}
}
