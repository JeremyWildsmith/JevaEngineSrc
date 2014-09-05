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
package io.github.jevaengine.rpgbase.server.library;

import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.netcommon.INetVisitor;
import io.github.jevaengine.rpgbase.Item;
import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.rpgbase.IItemStoreObserver;
import io.github.jevaengine.rpgbase.ILoadoutObserver;
import io.github.jevaengine.rpgbase.ItemIdentifier;
import io.github.jevaengine.rpgbase.DefaultItemSlot;
import io.github.jevaengine.rpgbase.character.CharacterInventory;
import io.github.jevaengine.rpgbase.character.CharacterLoadout;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.rpgbase.character.CharacterInventory.InventorySlot;
import io.github.jevaengine.rpgbase.dialogue.IDialogueRoute;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter.AddInventoryItem;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter.Attack;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter.EquipItem;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter.HealthSet;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter.Movement;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter.RemoveInventoryItem;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter.UnequipItem;
import io.github.jevaengine.rpgbase.server.RpgCharacterCertificate;
import io.github.jevaengine.rpgbase.server.ServerDialogueSession;
import io.github.jevaengine.server.IVisitAuthorizer;
import io.github.jevaengine.server.ServerEntity;
import io.github.jevaengine.server.ServerEntityCertificate;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.DefaultEntity;

import java.util.ArrayList;
import java.util.Iterator;

//Wrapper class to be used strictly for wrapping RpgCharacter entities.
@SharedClass(name = "RpgCharacter", policy = SharePolicy.ClientR)
public final class ServerRpgCharacter extends ServerEntity<RpgCharacter>
{
	private static final int SYNC_INTERVAL = 100;
	
	private Movement m_movementState;
	private Attack m_attackState;
	
	private ServerRpgCharacterObserver m_observer = new ServerRpgCharacterObserver();
	
	private ArrayList<ServerDialogueSession> m_activeDialogueSessions = new ArrayList<>();
	
	protected ServerRpgCharacter(RpgCharacter character)
	{
		super(character, SYNC_INTERVAL);
		
		m_observer = new ServerRpgCharacterObserver();
		
		character.addObserver(m_observer);
		character.addActionObserver(m_observer);
		character.addConditionObserver(m_observer);
		
		character.getInventory().addObserver(m_observer);
		character.getLoadout().addObserver(m_observer);

		m_movementState = new Movement(character.getLocation());
		m_attackState = new Attack(null);
	}
	
	@Override
	public void dispose()
	{
		RpgCharacter character = (RpgCharacter)getEntity();

		character.removeObserver(m_observer);
		character.removeActionObserver(m_observer);
		character.removeConditionObserver(m_observer);
		
		character.getInventory().removeObserver(m_observer);
		character.getLoadout().removeObserver(m_observer);
	}
	
	public void listenToDialogue(DefaultEntity speaker, IDialogueRoute route)
	{
		ServerDialogueSession session = new ServerDialogueSession(speaker, getEntity(), route);
		
		if(session.isActive())
		{
			m_activeDialogueSessions.add(session);
			share(session);
		}
	}
	
	@Override
	public void initializeRemote(Communicator sender)
	{
		RpgCharacter character = getEntity();
		
		send(sender, m_movementState);
		send(sender, new HealthSet(character.getHealth()));

		for (InventorySlot slot : character.getInventory().getSlots())
		{
			if (!slot.isEmpty())
				send(sender, new AddInventoryItem(slot.getIndex(), slot.getItem().getDescriptor()));
		}

		for (DefaultItemSlot slot : character.getLoadout().getSlots())
		{
			if (!slot.isEmpty())
				send(sender, new EquipItem(slot.getItem().getDescriptor().toString()));
		}
	}
	
	@Override
	protected IVisitAuthorizer<INetVisitor<RpgCharacter>, RpgCharacter> getDefaultVisitAuthorizer()
	{
		return new IVisitAuthorizer<INetVisitor<RpgCharacter>, RpgCharacter>() {
			@Override
			public boolean isVisitorAuthorized(INetVisitor<RpgCharacter> visitor, RpgCharacter target) {
				return false;
			}
		};
	}
	
	@Override
	protected ServerEntityCertificate<RpgCharacter> constructDefaultCertificate()
	{
		return new RpgCharacterCertificate(getEntity());
	}
	
	@Override
	protected void doLogic(int deltaTime)
	{
		Iterator<ServerDialogueSession> it = m_activeDialogueSessions.iterator();
		
		while(it.hasNext())
		{
			ServerDialogueSession session = it.next();
			session.update(deltaTime);
			
			if(!session.isActive())
			{
				it.remove();
				unshare(session);
			}
		}
	}
	
	@Override
	protected void doSynchronization() throws InvalidMessageException
	{
		for(ServerDialogueSession s : m_activeDialogueSessions)
			s.synchronize();
	}
	
	private class ServerRpgCharacterObserver implements RpgCharacter.IActionObserver,
														RpgCharacter.IConditionObserver,
														RpgCharacter.IEntityObserver,
														IItemStoreObserver,
														ILoadoutObserver
	{
		@Override
		public void healthChanged(int delta)
		{
			send(new HealthSet(getEntity().getHealth()));
		}

		@Override
		public void enterWorld()
		{
			m_movementState = new Movement(getEntity().getLocation());
		}

		@Override
		public void leaveWorld() { }

		@Override
		public void attack(@Nullable RpgCharacter attackee)
		{
			m_movementState = new Movement(getEntity().getLocation());
			m_attackState = new Attack(attackee == null ? null : attackee.getInstanceName());
			send(m_movementState);
			send(m_attackState);
		}

		@Override
		public void endMoving()
		{
			m_movementState = new Movement(getEntity().getLocation());
			send(m_movementState);
		}

		@Override
		public void movingTowards(Vector3F target)
		{
			m_movementState = new Movement(getEntity().getLocation(), target);
			send(m_movementState);
		}

		@Override
		public void flagSet(String name, int value) { }

		@Override
		public void flagCleared(String name) { }
		
		@Override
		public void beginMoving() { }

		@Override
		public void attacked(RpgCharacter attacker) { }

		@Override
		public void moved() { }
		
		@Override
		public void locationSet()
		{
			m_movementState = new Movement(getEntity().getLocation());
			send(m_movementState);
		}

		@Override
		public void addItem(int iSlot, ItemIdentifier item)
		{
			send(new AddInventoryItem(iSlot, item));
		}

		@Override
		public void removeItem(int iSlot, ItemIdentifier item)
		{
			send(new RemoveInventoryItem(iSlot));
		}

		@Override
		public void itemAction(int slotIndex, RpgCharacter accessor, String action) { }

		@Override
		public void unequip(ItemType gearType)
		{
			send(new UnequipItem(gearType));
		}

		@Override
		public void equip(Item item)
		{
			send(new EquipItem(item.getDescriptor().toString()));
		}
	}
}
