package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.netcommon.entity.NetEntityIdentifier;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession.DialogueQuery;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession.IDialogueSessionObserver;
import io.github.jevaengine.rpgbase.dialogue.IDialogueRoute;
import io.github.jevaengine.rpgbase.netcommon.dialogue.EndDialogue;
import io.github.jevaengine.rpgbase.netcommon.dialogue.QueryAnswer;
import io.github.jevaengine.rpgbase.netcommon.dialogue.Signal;
import io.github.jevaengine.server.ServerGame;
import io.github.jevaengine.world.entity.DefaultEntity;

public class ServerDialogueSession extends SharedEntity
{
	private static final int SYNC_INTERVAL = 50;
	
	private DialogueSession m_session;
	
	public ServerDialogueSession(DefaultEntity speaker, DefaultEntity listener, IDialogueRoute route)
	{
		super(SYNC_INTERVAL);
		m_session = new DialogueSession(speaker, listener, route);
		m_session.addObserver(new SessionObserver());
	}

	public void addObserver(IDialogueSessionObserver o)
	{
		m_session.addObserver(o);
	}
	
	public void removeObserver(IDialogueSessionObserver o)
	{
		m_session.removeObserver(o);
	}
	
	public boolean isActive()
	{
		return m_session.isActive();
	}
	
	@Override
	protected void doLogic(int deltaTime) { }

	@Override
	protected void doSynchronization() throws InvalidMessageException { }

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object recv) throws InvalidMessageException
	{
		if(recv instanceof Signal && (Signal)recv == Signal.InitializeRequest)
		{
			if(!m_session.isActive())
			{
				DialogueQuery query = m_session.getCurrentQuery();
				
				send(new QueryAnswer(new NetEntityIdentifier(query.getSpeaker().getInstanceName()),
									 new NetEntityIdentifier(query.getListener().getInstanceName()),
									 query.getQuery(),query.getAnswers()));

			}else
				send(Signal.EndDialogue);
			
		} if(recv instanceof INetVisitor<?>)
		{
			INetVisitor<?> genericVisitor = (INetVisitor<?>)recv;
			
			if(!genericVisitor.getHostComponentClass().isAssignableFrom(m_session.getClass()))
				throw new InvalidMessageException(sender, recv, "Visitor cannot visit this component due to incompatable host component types");
			
			@SuppressWarnings("unchecked")
			INetVisitor<DialogueSession> sessionVisitor = (INetVisitor<DialogueSession>)genericVisitor;
			
			if (Core.getService(ServerGame.class).getVisitAuthorizationPool().isVisitAuthorized(sender, sessionVisitor, m_session))
				sessionVisitor.visit(sender, m_session, true);
			else
				throw new InvalidMessageException(sender, recv, "Dispatcher of visitor did not have sufficient ownership to perform task");			
		} else
			throw new InvalidMessageException(sender, recv, "Unrecognized message from sender.");
		
		return true;
	}
	
	private class SessionObserver implements IDialogueSessionObserver
	{
		@Override
		public void speakerInquired(DialogueQuery query)
		{
			send(new QueryAnswer(new NetEntityIdentifier(query.getSpeaker().getInstanceName()),
				 new NetEntityIdentifier(query.getListener().getInstanceName()),
				 query.getQuery(), query.getAnswers()));
		}

		@Override
		public void listenerSaid(String message) { }

		@Override
		public void end()
		{
			send(new EndDialogue());
		}

		@Override
		public void eventRaised(int event) { }
	}
}
