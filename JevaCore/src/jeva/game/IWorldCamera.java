/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
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
