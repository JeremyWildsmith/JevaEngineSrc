package jeva.world;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.util.List;

import jeva.math.Vector2F;

/**
 * The Interface ILight.
 */
public interface ILight
{

	/**
	 * Render composite.
	 * 
	 * @param g
	 *            the g
	 * @param lightingArea
	 *            the lighting area
	 * @param ambient
	 *            the ambient
	 * @param obstructedLightingArea
	 *            the obstructed lighting area
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param fScale
	 *            the f scale
	 */
	void renderComposite(Graphics2D g, Area lightingArea, Color ambient, List<LightObstruction> obstructedLightingArea, int x, int y, float fScale);

	/**
	 * Render light.
	 * 
	 * @param g
	 *            the g
	 * @param lightingArea
	 *            the lighting area
	 * @param ambient
	 *            the ambient
	 * @param obstructedLightingArea
	 *            the obstructed lighting area
	 * @param x
	 *            the x
	 * @param y
	 *            the y
	 * @param fScale
	 *            the f scale
	 */
	void renderLight(Graphics2D g, Area lightingArea, Color ambient, List<LightObstruction> obstructedLightingArea, int x, int y, float fScale);

	/**
	 * Gets the allocation.
	 * 
	 * @param fWorldScale
	 *            the f world scale
	 * @param offsetX
	 *            the offset x
	 * @param offsetY
	 *            the offset y
	 * @return the allocation
	 */
	Area getAllocation(float fWorldScale, int offsetX, int offsetY);

	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	Vector2F getLocation();
}
