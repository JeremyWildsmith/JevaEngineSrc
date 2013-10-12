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

import jeva.math.Vector2D;
import jeva.world.Route;

import com.sun.istack.internal.Nullable;

/**
 * The Interface IRoutingRules.
 */
public interface IRoutingRules
{

	/**
	 * Gets the movements.
	 * 
	 * @param currentNode
	 *            the current node
	 * @param destination
	 *            the destination
	 * @return the movements
	 * @throws IncompleteRouteException
	 *             the incomplete route exception
	 */
	public WorldDirection[] getMovements(Route.SearchNode currentNode, @Nullable Vector2D destination) throws IncompleteRouteException;
}
