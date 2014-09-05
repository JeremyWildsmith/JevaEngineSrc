package io.github.jevaengine.graphics;

import io.github.jevaengine.math.Rect2D;

import java.awt.Graphics2D;

public interface IFont
{
	Rect2D getTextBounds(String text);
	Rect2D drawText(Graphics2D g, int x, int y, float scale, String text);
	Rect2D getMaxCharacterBounds();
	boolean doesMappingExists(char keyChar);
}
