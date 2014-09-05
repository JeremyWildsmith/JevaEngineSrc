package io.github.jevaengine.ui.style;

import io.github.jevaengine.audio.NullAudioClip;
import io.github.jevaengine.graphics.NullFont;

public final class NullUIStyle implements IUIStyle
{
	@Override
	public ComponentStyle getComponentStyle(String componentName)
	{
		ComponentStateStyle stateStyle = new ComponentStateStyle(new NullFont(), new NullFrameFactory(), new NullAudioClip());
	
		return new ComponentStyle(stateStyle, stateStyle, stateStyle);
	}
	
	@Override
	public void dispose() { }
}
