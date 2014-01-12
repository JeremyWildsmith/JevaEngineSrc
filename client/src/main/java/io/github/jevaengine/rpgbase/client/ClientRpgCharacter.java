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
package io.github.jevaengine.rpgbase.client;

import io.github.jevaengine.Core;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.AttackTask;
import io.github.jevaengine.rpgbase.DialogueController;
import io.github.jevaengine.rpgbase.Inventory;
import io.github.jevaengine.rpgbase.Item;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.RpgGame;
import io.github.jevaengine.rpgbase.netcommon.NetRpgCharacter;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.Entity.IEntityObserver;
import io.github.jevaengine.world.MovementTask;

@SharedClass(name = "RpgCharacter", policy = SharePolicy.ClientR)
public final class ClientRpgCharacter extends ClientEntity<RpgCharacter>
{
	private @Nullable ClientRpgCharacterTaskFactory m_taskFactory;
	
	private DialogueController m_dialogueController;
	
	private DialogueEventDispatcher m_dialogueEventDispatcher = new DialogueEventDispatcher();
	
	public ClientRpgCharacter()
	{
		super(RpgCharacter.class);
		
		m_dialogueController = Core.getService(RpgGame.class).getDialogueController();
	}


	@Override
	public void beginVisit()
	{
		if(m_taskFactory != null)
			m_taskFactory.setVisitorMutation(true);
	}

	@Override
	public void endVisit()
	{

		if(m_taskFactory != null)
			m_taskFactory.setVisitorMutation(false);
	}

	@Override
	public void entityCreated()
	{
		RpgCharacter character = getEntity();
		character.getInventory().addObserver(new ServerRpgCharacterInventoryObserver());
		character.addObserver(new EntityObserver());
		
		m_taskFactory = new ClientRpgCharacterTaskFactory();
		
		if(isOwner())
			getEntity().setTaskFactory(m_taskFactory);
	}
	
	private class EntityObserver implements IEntityObserver
	{

		@Override
		public void enterWorld()
		{
			m_dialogueController.addObserver(m_dialogueEventDispatcher);
		}

		@Override
		public void leaveWorld()
		{
			m_dialogueController.removeObserver(m_dialogueEventDispatcher);
		}

		@Override
		public void moved() { }

		@Override
		public void flagSet(String name, int value) { }

		@Override
		public void flagCleared(String name) { }
		
	}
	
	private class DialogueEventDispatcher implements DialogueController.IDialogueControlObserver
	{
		@Override
		public void dialogueEvent(int event)
		{
			Entity speaker = m_dialogueController.getSpeaker();
			Entity listener = m_dialogueController.getListener();
			
			//Am I the speaker?
			if(speaker == getEntity())
				send(new NetRpgCharacter.DialogueEvent(event, listener.getInstanceName()));
		}
		
		@Override
		public void beginDialogue() { }

		@Override
		public void endDialogue() { }

		@Override
		public void speakerSaid(String message) { }

		@Override
		public void listenerSaid(String message) { }
		
	}
	
	private class ServerRpgCharacterInventoryObserver implements Inventory.IInventoryObserver
	{

		@Override
		public void addItem(int slotIndex, Item.ItemIdentifer item) { }

		@Override
		public void removeItem(int slotIndex, Item.ItemIdentifer item) { }

		@Override
		public void itemAction(int slotIndex, RpgCharacter accessor, String action)
		{
			send(new NetRpgCharacter.InventoryAction(accessor, action, slotIndex));
		}
	}
	
	private class ClientRpgCharacterTaskFactory extends RpgCharacter.RpgCharacterTaskFactory
	{
		private boolean m_visitorMutation = false;
		
		@Override
		public MovementTask createMovementTask(final RpgCharacter host, final @Nullable Vector2F dest, final float fRadius)
		{
			if(m_visitorMutation)
			{
				return new MovementTask(host.getSpeed())
				{
					
					@Override
					protected boolean blocking() { return true; }
					
					@Override
					protected Vector2F getDestination()
					{
						return new Vector2F(dest);
					}
					
					@Override
					protected boolean atDestination()
					{
						return host.getLocation().equals(dest);
					}
				};
			}
			
			if(dest != null)
				send(new NetRpgCharacter.QueryMoveTo(dest));
			
			return new MovementTask(fRadius)
			{
				@Override
				protected boolean blocking()
				{
					return true;
				}
				
				@Override
				protected Vector2F getDestination() { return host.getLocation(); }
				
				@Override
				protected boolean atDestination() { return true; }
			};
		}
		
		@Override
		public AttackTask createAttackTask(final RpgCharacter host, final @Nullable RpgCharacter target)
		{
			if(m_visitorMutation)
				return super.createAttackTask(host, target);
			
			if(target != null)
				send(new NetRpgCharacter.Attack(target.getInstanceName()));
			
			return new AttackTask(target)
			{
				@Override
				public boolean doAttack(RpgCharacter attackee) { return false; }
			};
		}
		
		public void setVisitorMutation(boolean isVisitor)
		{
			m_visitorMutation = isVisitor;
		}
	}
}
