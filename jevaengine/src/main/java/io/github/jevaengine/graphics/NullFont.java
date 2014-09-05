package io.github.jevaengine.graphics;

import io.github.jevaengine.math.Rect2D;

import java.awt.Graphics2D;

public final class NullFont implements IFont
{
	@Override
	public Rect2D getTextBounds(String text)
	{
		return new Rect2D(1, 1);
	}

	@Override
	public Rect2D drawText(Graphics2D g, int x, int y, float scale, String text)
	{
		//Avoids divide by zero exception when being used.
		return new Rect2D(1, 1);
	}

	@Override
	public Rect2D getMaxCharacterBounds()
	{
		//Avoids divide by zero exception when being used.
		return new Rect2D(1, 1);
	}

	@Override
	public boolean doesMappingExists(char keyChar)
	{
		return false;
	}
}
