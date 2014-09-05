package io.github.jevaengine.rpgbase.client;

import io.github.jevaengine.client.AuthenticatingState;
import io.github.jevaengine.client.ClientGame;
import io.github.jevaengine.client.ClientWorld;
import io.github.jevaengine.client.DisconnectedState;
import io.github.jevaengine.client.IClientGameStateFactory;
import io.github.jevaengine.client.LoadingWorldState;
import io.github.jevaengine.client.LoginState;
import io.github.jevaengine.client.PlayingState;
import io.github.jevaengine.client.SelectWorldState;
import io.github.jevaengine.game.IGameScriptProvider;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.netcommon.user.UserCredentials;
import io.github.jevaengine.rpgbase.IRpgGame;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.util.Nullable;

public class RpgClientGame extends ClientGame implements IRpgGame
{
	@Nullable
	private RpgCharacter m_playerCharacter;
	
	@Override
	public IClientGameStateFactory getStateFactory()
	{
		return new RpgClientGameStateFactory();
	}

	@Override
	public IGameScriptProvider getScriptProvider() {
		return new RpgClientGameStateFactory();
	}

	@Override
	protected Sprite getCursor() {
		// TODO Auto-generated method stub
		return null;
	}
	
	void setPlayerCharacter(RpgCharacter playerCharacter)
	{
		m_playerCharacter = playerCharacter;
	}
	
	public static class RpgClientGameStateFactory implements IClientGameStateFactory
	{
		private RpgClientGameStateFactory() { }

		@Override
		public LoginState createLoginState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public AuthenticatingState createAuthenticatingState(UserCredentials credentials) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SelectWorldState createSelectWorldState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public LoadingWorldState createLoadingWorldState(String worldName) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PlayingState createPlayingState(ClientWorld world) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public DisconnectedState createDisconnectedState(String reason) {
			// TODO Auto-generated method stub
			return null;
		}	
	}
}
