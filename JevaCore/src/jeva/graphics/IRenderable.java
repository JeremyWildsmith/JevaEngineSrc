package jeva.graphics;

import java.awt.Graphics2D;

/**
 * The Interface IRenderable.
 */
public interface IRenderable
{
	/**
	 * Render.
	 * 
	 * @param g
	 *            the g
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param fScale
	 *            the f scale
	 */
	void render(Graphics2D g, int x, int y, float fScale);
}
