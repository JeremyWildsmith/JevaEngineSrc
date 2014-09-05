package io.github.jevaengine.client;

import io.github.jevaengine.netcommon.user.UserCredentials;

public interface IClientGameStateFactory
{
	LoginState createLoginState();
	AuthenticatingState createAuthenticatingState(UserCredentials credentials);
	SelectWorldState createSelectWorldState();
	LoadingWorldState createLoadingWorldState(String worldName);
	PlayingState createPlayingState(ClientWorld world);
	DisconnectedState createDisconnectedState(String reason);
}
