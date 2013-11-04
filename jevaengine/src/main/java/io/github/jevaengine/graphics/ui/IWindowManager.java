package io.github.jevaengine.graphics.ui;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.joystick.InputManager;

public interface IWindowManager extends IRenderable
{
	void addWindow(Window window);

	void removeWindow(Window window);

	void onMouseEvent(InputManager.InputMouseEvent mouseEvent);

	boolean onKeyEvent(InputManager.InputKeyEvent keyEvent);

	void update(int deltaTime);
}
