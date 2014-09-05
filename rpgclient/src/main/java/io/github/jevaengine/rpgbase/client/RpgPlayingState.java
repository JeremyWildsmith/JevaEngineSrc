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
import io.github.jevaengine.client.ClientEntityCertificate;
import io.github.jevaengine.client.ClientEntityCertificate.IClientEntityCertificateObserver;
import io.github.jevaengine.client.ClientGame;
import io.github.jevaengine.client.ClientUser;
import io.github.jevaengine.client.ClientUser.IClientUserCertificatesObserver;
import io.github.jevaengine.client.ClientWorld;
import io.github.jevaengine.client.LoadingWorldState;
import io.github.jevaengine.client.PlayingState;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.rpgbase.netcommon.NetEntityRightsCertificate.EntityCertificatePurpose;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.World.IWorldObserver;
import io.github.jevaengine.world.entity.DefaultEntity;

public class RpgPlayingState extends PlayingState
{
	private PlayerCharacterHandler m_playerCharacterCertificateMonitor = new PlayerCharacterHandler();

	private RpgCharacter m_playerCharacter;
	
	private ClientUser m_user;
	
	public RpgPlayingState(ClientWorld world)
	{
		super(world);
	}

	private void setPlayerCharacter(RpgCharacter character)
	{
		RpgClientGame game = Core.getService(RpgClientGame.class);
		
		m_playerCharacter = character;
		game.setPlayerCharacter(character);
	}
	
	@Override
	public void onEnter()
	{
		m_user = Core.getService(ClientGame.class).getUser();
		m_playerCharacterCertificateMonitor.begin();
	}

	@Override
	public void onLeave()
	{
		m_playerCharacterCertificateMonitor.end();	
	}
	
	@Nullable
	protected final RpgCharacter getPlayerCharacter()
	{
		return m_playerCharacter;
	}
	
	private class PlayerCharacterHandler
	{
		@Nullable
		private ClientEntityCertificate m_playerCertificate;

		private RightsObserver m_rightsObserver = new RightsObserver();
		private WorldObserver m_worldObserver = new WorldObserver();
		
		public void begin()
		{
			m_user.addCertificatesObserver(m_rightsObserver);
			getClientWorld().getWorld().addObserver(m_worldObserver);
			checkCertificates();
			checkForPlayerCharacter();
		}
		
		public void end()
		{
			m_user.removeEntityRightsCertificationObserver(m_rightsObserver);
			m_world.getClientWorld().removeObserver(m_worldObserver);
			resetCertification();
			setPlayerCharacter(null);
		}

		private ClientEntityRightsCertificate getPlayerCertificate()
		{
			for(ClientEntityRightsCertificate certificate : m_user.getEntityRightsCertificates())
			{
				if(certificate.getPurpose() == EntityCertificatePurpose.Player)
					return certificate;
			}
			
			return null;
		}
		
		private void checkCertificates()
		{
			ClientEntityRightsCertificate certificate = getPlayerCertificate();
			
			if(certificate != m_playerRightsCertificate)
			{
				if(m_playerRightsCertificate != null)
					m_playerRightsCertificate.removeObserver(m_rightsObserver);

				m_playerRightsCertificate = null;
				
				if(certificate != null)
				{
					if(!certificate.getHostWorld().equals(m_world.getWorldName()))
					{
						m_context.setState(new LoadingWorldState(m_style, m_user, certificate.getHostWorld().getFormalName()));
					}else
					{
						m_playerRightsCertificate = certificate;
						certificate.addObserver(m_rightsObserver);
					}
				}
			}
		}
		
		private void resetCertification()
		{
			if(m_playerRightsCertificate != null)
				m_playerRightsCertificate.removeObserver(m_rightsObserver);
			
			m_playerRightsCertificate = null;
		}
		
		public void checkForPlayerCharacter()
		{
			if(m_playerRightsCertificate == null)
				return;
			
			DefaultEntity character = m_world.getClientWorld().getEntities().getByName(m_playerRightsCertificate.getEntityName());
			
			if(character instanceof RpgCharacter)
				setPlayerCharacter((RpgCharacter)m_world.getClientWorld().getEntities().getByName(m_playerRightsCertificate.getEntityName()));
			else
				setPlayerCharacter(null);
		}
		
		private class WorldObserver implements IWorldObserver
		{
			@Override
			public void addedEntity(DefaultEntity e)
			{
				if(m_playerRightsCertificate != null &&
						e.getInstanceName().equals(m_playerRightsCertificate.getEntityName()) &&
						e instanceof RpgCharacter)
				{
					setPlayerCharacter((RpgCharacter)e);
				}
			}

			@Override
			public void removedEntity(DefaultEntity e)
			{
				if(m_playerRightsCertificate != null && e.getInstanceName().equals(m_playerRightsCertificate.getEntityName()))
					setPlayerCharacter(null);
			}
		}
		
		private class RightsObserver implements IClientUserCertificatesObserver, IClientEntityCertificateObserver
		{		
			@Override
			public void entityCertificationChanged()
			{
				checkCertificates();
				checkForPlayerCharacter();
			}
			
			@Override
			public void contextChanged()
			{
				if(!m_playerRightsCertificate.getHostWorld().equals(m_world.getWorldName()))
					m_context.setState(getContext().getStateFactory().createSelectWorldState());
			}
		}
	}
}
