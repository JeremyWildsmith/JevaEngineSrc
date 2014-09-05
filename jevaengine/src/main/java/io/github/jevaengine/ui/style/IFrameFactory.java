package io.github.jevaengine.ui.style;

import io.github.jevaengine.graphics.IImmutableGraphic;

public interface IFrameFactory
{
	IImmutableGraphic create(int desiredWidth, int desiredHeight);
}
