package io.github.jevaengine.rpgbase.server.library;

import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.rpgbase.RpgLibrary;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.rpgbase.server.RpgCharacterCertificate;
import io.github.jevaengine.server.ServerEntity;
import io.github.jevaengine.server.library.IServerLibrary;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.DefaultEntity;

import java.util.ArrayList;
import java.util.List;

public class RpgServerLibrary extends RpgLibrary implements IServerLibrary
{	
	@Override
	public List<Class<? extends SharedEntity>> getSharedClasses()
	{
		List<Class<? extends SharedEntity>> sharedClasses = new ArrayList<>();
		sharedClasses.add(ServerRpgCharacter.class);
		sharedClasses.add(RpgCharacterCertificate.class);
		
		return sharedClasses;
	}
	
	@Override
	@Nullable
	public <T extends DefaultEntity> IServerEntityWrapFactory getServerEntityWrapFactory(Class<T> entityClass)
	{
		if(entityClass.equals(RpgCharacter.class))
		{
			return new ServerRpgCharacterWrapFactory();
		}else
			return null;
	}
	
	public final class ServerRpgCharacterWrapFactory implements IServerEntityWrapFactory
	{
		@Override
		public ServerEntity<? extends DefaultEntity> wrap(DefaultEntity entity)
		{				
			return new ServerRpgCharacter((RpgCharacter)entity);
		}
	}
}
