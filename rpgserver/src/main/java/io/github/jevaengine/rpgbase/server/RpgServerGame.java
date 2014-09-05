package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.rpgbase.IRpgGame;
import io.github.jevaengine.server.ServerGame;
import io.github.jevaengine.world.entity.DefaultEntity.DefaultEntityBridge;

public class RpgServerGame extends ServerGame implements IRpgGame
{

	@Override
	public RpgServerScriptProvider getScriptProvider()
	{
		return new RpgServerScriptProvider();
	}
	
	public class RpgServerScriptProvider extends ServerGameScriptProvider implements IRpgGameScriptProvider
	{
		@Override
		public RpgServerGameBridge getBridge()
		{
			return new RpgServerGameBridge();
		}
	
		public class RpgServerGameBridge extends ServerGameBridge implements IRpgGameBridge
		{
			@Override
			public DefaultEntityBridge<?> getPlayer()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void initiateDialogue(DefaultEntityBridge<?> speaker, DefaultEntityBridge<?> listener, String dialoguePath)
			{
				throw new UnsupportedOperationException();
			}
		}
	}

}
