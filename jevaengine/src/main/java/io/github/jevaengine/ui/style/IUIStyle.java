package io.github.jevaengine.ui.style;

import io.github.jevaengine.IDisposable;

public interface IUIStyle extends IDisposable
{
	ComponentStyle getComponentStyle(String componentName);
}
