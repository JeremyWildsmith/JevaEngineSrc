package jevarpg.net.client;

public interface IGameState
{
	void enter(ClientGame context);

	void leave();

	void update(int deltaTime);
}
