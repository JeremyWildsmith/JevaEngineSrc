package io.github.jevaengine.ui;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.style.ComponentStyle;
import io.github.jevaengine.ui.style.IUIStyle;

public interface IImmutableControl extends IRenderable
{
	boolean hasFocus();
	
	String getInstanceName();
	
	Vector2D getLocation();
	Vector2D getAbsoluteLocation();
	
	ComponentStyle getComponentStyle();
	IUIStyle getStyle();
	
	IImmutableControl getParent();
	
	boolean isVisible();
	
	boolean onMouseEvent(InputMouseEvent mouseEvent);
	boolean onKeyEvent(InputKeyEvent keyEvent);
	
	Rect2D getBounds();
	
	void update(int deltaTime);
}
