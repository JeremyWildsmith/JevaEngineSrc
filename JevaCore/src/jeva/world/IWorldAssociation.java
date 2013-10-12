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
package jeva.world;

/**
 * The Interface IWorldAssociation.
 */
public interface IWorldAssociation
{

	/**
	 * Checks if is associated.
	 * 
	 * @return true, if is associated
	 */
	boolean isAssociated();

	/**
	 * Disassociate.
	 */
	void disassociate();

	/**
	 * Associate.
	 * 
	 * @param world
	 *            the world
	 */
	void associate(World world);
}
