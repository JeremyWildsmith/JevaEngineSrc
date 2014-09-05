package io.github.jevaengine.rpgbase.dialogue;


public interface IDialogueSpeakerSession
{
	void cancel();
	
	boolean isActive();
	
	public void addObserver(IDialogueSpeakerSessionObserver observer);
	public void removeObserver(IDialogueSpeakerSessionObserver observer);
	
	public interface IDialogueSpeakerSessionObserver
	{
		void listenerSaid(String message);
		void end();
		void eventRaised(int event);
	}
}
