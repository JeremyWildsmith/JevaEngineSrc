package io.github.jevaengine.ui.style;

import io.github.jevaengine.graphics.IImmutableGraphic;
import io.github.jevaengine.graphics.NullGraphic;

public final class NullFrameFactory implements IFrameFactory
{
	@Override
	public IImmutableGraphic create(int desiredWidth, int desiredHeight)
	{
		return new NullGraphic(desiredWidth, desiredHeight);
	}
}