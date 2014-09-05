package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.Core;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession;
import io.github.jevaengine.rpgbase.netcommon.certificate.rpgcharacter.CertificatePermissionLevel;
import io.github.jevaengine.rpgbase.netcommon.certificate.rpgcharacter.UpdateCertificatePermissionLevel;
import io.github.jevaengine.rpgbase.netcommon.dialogue.EndDialogue;
import io.github.jevaengine.rpgbase.netcommon.dialogue.SubmitAnswer;
import io.github.jevaengine.rpgbase.netcommon.rpgcharacter.Attack;
import io.github.jevaengine.rpgbase.netcommon.rpgcharacter.EquipItem;
import io.github.jevaengine.rpgbase.netcommon.rpgcharacter.InventoryAction;
import io.github.jevaengine.rpgbase.netcommon.rpgcharacter.QueryMoveTo;
import io.github.jevaengine.rpgbase.netcommon.rpgcharacter.UnequipItem;
import io.github.jevaengine.server.IVisitAuthorizer;
import io.github.jevaengine.server.ServerEntityCertificate;
import io.github.jevaengine.server.ServerGame;

import java.util.HashSet;

@SharedClass(name = "RpgCharacterCertificate", policy = SharePolicy.ClientR)
public class RpgCharacterCertificate extends ServerEntityCertificate<RpgCharacter>
{
	private RpgCharacterVisitAuthorizer m_characterVisitAuthorizer = new RpgCharacterVisitAuthorizer();
	private DialogueSessionVisitAuthorizer m_dialogueVisitAuthorizer = new DialogueSessionVisitAuthorizer();
	
	private CertificatePermissionLevel m_permissionLevel = CertificatePermissionLevel.None;
	
	public RpgCharacterCertificate(RpgCharacter entity)
	{
		super(entity, new RpgCharacterCertificateBridge());
	}
	
	private void setPermissionLevel(CertificatePermissionLevel level)
	{
		switch(level)
		{
		
		}
		
		send(new UpdateCertificatePermissionLevel(m_permissionLevel));
	}

	@Override
	protected void authorizeCommunicator(Communicator communicator)
	{
		Core.getService(ServerGame.class).getVisitAuthorizationPool().addVisitAuthorizer(communicator, RpgCharacter.class, m_characterVisitAuthorizer);
		Core.getService(ServerGame.class).getVisitAuthorizationPool().addVisitAuthorizer(communicator, DialogueSession.class, m_dialogueVisitAuthorizer);
	}

	@Override
	protected void deauthorizeCommunicator(Communicator communicator)
	{
		Core.getService(ServerGame.class).getVisitAuthorizationPool().removeVisitAuthorizer(communicator, RpgCharacter.class, m_characterVisitAuthorizer);
		Core.getService(ServerGame.class).getVisitAuthorizationPool().removeVisitAuthorizer(communicator, DialogueSession.class, m_dialogueVisitAuthorizer);		
	}
	
	@Override
	protected void initializeRemote(Communicator sender)
	{
		super.initializeRemote(sender);
		send(sender, new UpdateCertificatePermissionLevel(m_permissionLevel));
	}

	public class RpgCharacterVisitAuthorizer implements IVisitAuthorizer<INetVisitor<RpgCharacter>, RpgCharacter>
	{
		private HashSet<Class<? extends INetVisitor<RpgCharacter>>> m_authorizedVisitors = new HashSet<>();
		
		@Override
		public boolean isVisitorAuthorized(INetVisitor<RpgCharacter> visitor, RpgCharacter target)
		{
			if(target != RpgCharacterCertificate.this.getTarget())
				return false;
			
			return m_authorizedVisitors.contains(visitor.getClass());
		}
		
		public void setAllowPlayerVisitors(boolean allowPlayerVisitors)
		{
			if(allowPlayerVisitors)
			{
				m_authorizedVisitors.add(Attack.class);
				m_authorizedVisitors.add(EquipItem.class);
				m_authorizedVisitors.add(UnequipItem.class);
				m_authorizedVisitors.add(InventoryAction.class);
				m_authorizedVisitors.add(QueryMoveTo.class);
			}else
			{
				m_authorizedVisitors.remove(Attack.class);
				m_authorizedVisitors.remove(EquipItem.class);
				m_authorizedVisitors.remove(UnequipItem.class);
				m_authorizedVisitors.remove(InventoryAction.class);
				m_authorizedVisitors.remove(QueryMoveTo.class);
			}
		}
	}
	
	public class DialogueSessionVisitAuthorizer implements IVisitAuthorizer<INetVisitor<DialogueSession>, DialogueSession>
	{
		private HashSet<Class<? extends INetVisitor<DialogueSession>>> m_authorizedVisitors = new HashSet<>();
		
		@Override
		public boolean isVisitorAuthorized(INetVisitor<DialogueSession> visitor, DialogueSession target)
		{
			if(target.getCurrentQuery().getListener() != getTarget())
				return false;
			else
				return m_authorizedVisitors.contains(visitor.getClass());
		}
		
		public void setAllowDialogueInteraction(boolean allowDiaogueInteractions)
		{
			if(allowDiaogueInteractions)
			{
				m_authorizedVisitors.add(SubmitAnswer.class);
				m_authorizedVisitors.add(EndDialogue.class);
			}
		}
	}
	
	public static class RpgCharacterCertificateBridge extends ServerEntityCertificateBridge<RpgCharacterCertificate, RpgCharacter>
	{
		public void setPermissionLevelNone()
		{
			getCertificate().setPermissionLevel(CertificatePermissionLevel.None);
			getCertificate().m_characterVisitAuthorizer.setAllowPlayerVisitors(true);
			getCertificate().m_dialogueVisitAuthorizer.setAllowDialogueInteraction(true);
		}
		
		public void setPermissionLevelPlayer()
		{
			getCertificate().setPermissionLevel(CertificatePermissionLevel.Player);
			getCertificate().m_permissionLevel = CertificatePermissionLevel.Player;
			getCertificate().m_characterVisitAuthorizer.setAllowPlayerVisitors(true);
			getCertificate().m_dialogueVisitAuthorizer.setAllowDialogueInteraction(true);
		}
	}
}
