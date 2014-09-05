package io.github.jevaengine.game;

import io.github.jevaengine.ui.WindowManager;

public interface IGame
{	
	void render(IRenderer r);
	void update(int deltaTime);
	
	WindowManager getWindowManager();
}
