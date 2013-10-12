package jeva.game;

import jeva.math.Vector2D;
import jeva.world.World;

/**
 * The Interface IWorldCamera.
 */
public interface IWorldCamera
{
	/**
	 * Gets the look at.
	 * 
	 * @return the look at
	 */
	Vector2D getLookAt();

	/**
	 * Gets the scale.
	 * 
	 * @return the scale
	 */
	float getScale();

	/**
	 * Attach.
	 * 
	 * @param world
	 *            the world
	 */
	void attach(World world);

	/**
	 * Dettach.
	 */
	void dettach();
}
