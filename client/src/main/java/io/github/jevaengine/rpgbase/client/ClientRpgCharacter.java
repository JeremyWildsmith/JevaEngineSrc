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
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.communication.Communicator;
import io.github.jevaengine.communication.InvalidMessageException;
import io.github.jevaengine.communication.SharePolicy;
import io.github.jevaengine.communication.SharedClass;
import io.github.jevaengine.communication.SharedEntity;
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
import io.github.jevaengine.world.IWorldAssociation;
import io.github.jevaengine.world.MovementTask;
import io.github.jevaengine.world.World;

@SharedClass(name = "RpgCharacter", policy = SharePolicy.ClientR)
public final class ClientRpgCharacter extends NetRpgCharacter implements IWorldAssociation, IClientShared
{
	private static final int SYNC_INTERVAL = 10;

	private volatile RpgCharacter m_character;
	private volatile boolean m_isLoading = false;
	
	private boolean m_dispatchedInit = false;
	
	private int m_tickCount = 0;

	private World m_world;
	
	private @Nullable ClientRpgCharacterTaskFactory m_taskFactory;
	
	private DialogueController m_dialogueController;
	
	public ClientRpgCharacter()
	{
		m_dialogueController = Core.getService(RpgGame.class).getDialogueController();
	}

	@Override
	public SharedEntity getSharedEntity()
	{
		return this;
	}

	protected RpgCharacter getCharacter()
	{
		return m_character;
	}

	@Override
	public boolean isAssociated()
	{
		if (m_character != null)
			return m_character.isAssociated();
		else
			return false;
	}

	@Override
	public void disassociate()
	{
		if (m_character != null && m_character.isAssociated())
			m_character.getWorld().removeEntity(m_character);

		m_world = null;
	}

	@Override
	public void associate(World world)
	{
		m_world = world;

		if (m_character != null)
			m_world.addEntity(m_character);
	}

	@Override
	public void update(int deltaTime)
	{
		if (!m_dispatchedInit)
		{
			m_dispatchedInit = true;
			send(PrimitiveQuery.Initialize);
		}

		if (m_isLoading && m_character != null)
		{
			if (m_world != null)
				m_world.addEntity(m_character);

			m_isLoading = false;
		}

		m_tickCount += deltaTime;
		if (m_tickCount >= SYNC_INTERVAL)
		{
			m_tickCount = 0;
			snapshot();
		}
	}

	@Override
	protected boolean onMessageRecieved(Communicator sender, Object message) throws InvalidMessageException
	{
		if (message instanceof InitializationArguments)
		{
			final InitializationArguments args = (InitializationArguments) message;

			if (m_isLoading || m_character != null)
				throw new InvalidMessageException(sender, message, "Server dispatched initilization twice");

			m_isLoading = true;

			new Thread()
			{
				@Override
				public void run()
				{
					if(args.isClientOwned())
						m_dialogueController.addObserver(new DialogueEventDispatcher());
					
					m_taskFactory = new ClientRpgCharacterTaskFactory();
					
					if (args.getName() == null)
						m_character = new RpgCharacter(Core.getService(IResourceLibrary.class).openConfiguration(args.getConfiguration()),
														m_taskFactory);
					else
						m_character = new RpgCharacter(args.getName(), Core.getService(IResourceLibrary.class).openConfiguration(args.getConfiguration()),
											m_taskFactory);
					
					m_character.getInventory().addObserver(new ServerRpgCharacterInventoryObserver());
				}
			}.start();

			return true;
		} else if (m_character == null || !isAssociated())
			return false;

		if (message instanceof PrimitiveQuery)
		{
			switch ((PrimitiveQuery) message)
			{
				case Initialize:
					throw new InvalidMessageException(sender, message, "Invalid message recieved from server.");
				default:
					throw new InvalidMessageException(sender, message, "Unrecognized message recieved from server.");
			}
		} else if (message instanceof IRpgCharacterVisitor)
		{
			IRpgCharacterVisitor visitor = (IRpgCharacterVisitor) message;
			
			m_taskFactory.visitLocally(sender, visitor);
		}
		else
			throw new InvalidMessageException(sender, message, "Unrecognized message recieved from server.");

		return true;
	}

	private class DialogueEventDispatcher implements DialogueController.IDialogueControlObserver
	{
		@Override
		public void dialogueEvent(int event)
		{
			Entity speaker = m_dialogueController.getSpeaker();
			
			send(new DialogueEvent(event, speaker == null ? null : speaker.getInstanceName()));
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
			send(new InventoryAction(accessor, action, slotIndex));
		}
	}
	
	private class ClientRpgCharacterTaskFactory extends RpgCharacter.RpgCharacterTaskFactory
	{
		private boolean m_localMutation = false;
		@Override
		public MovementTask createMovementTask(final RpgCharacter host, final @Nullable Vector2F dest, final float fRadius)
		{
			if(m_localMutation)
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
				send(new QueryMoveTo(dest));
			
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
			if(m_localMutation)
				return super.createAttackTask(host, target);
			
			if(target != null)
				send(new Attack(target.getInstanceName()));
			
			return new AttackTask(target)
			{
				@Override
				public boolean doAttack(RpgCharacter attackee) { return true; }
			};
		}
		
		public void visitLocally(Communicator sender, IRpgCharacterVisitor visitor) throws InvalidMessageException
		{
			m_localMutation = true;
			visitor.visit(sender, m_character);
			m_localMutation = false;
		}
	}
}
