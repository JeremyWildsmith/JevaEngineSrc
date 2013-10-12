package jeva.world;

import java.awt.geom.Area;

import jeva.math.Vector2F;

/**
 * The Class LightObstruction.
 */
public class LightObstruction
{

	/** The m_area. */
	private Area m_area;

	/** The m_direction. */
	private Vector2F m_direction;

	/** The m_f visibility factor. */
	private float m_fVisibilityFactor;

	/**
	 * Instantiates a new light obstruction.
	 * 
	 * @param area
	 *            the area
	 * @param direction
	 *            the direction
	 * @param fVisibilityFactor
	 *            the f visibility factor
	 */
	public LightObstruction(Area area, Vector2F direction, float fVisibilityFactor)
	{
		m_area = area;
		m_direction = (direction.isZero() ? direction : direction.normalize());
		m_fVisibilityFactor = fVisibilityFactor;
	}

	/**
	 * Gets the area.
	 * 
	 * @return the area
	 */
	public Area getArea()
	{
		return m_area;
	}

	/**
	 * Gets the direction.
	 * 
	 * @return the direction
	 */
	public Vector2F getDirection()
	{
		return m_direction;
	}

	/**
	 * Gets the visibility factor.
	 * 
	 * @return the visibility factor
	 */
	public float getVisibilityFactor()
	{
		return m_fVisibilityFactor;
	}
}
