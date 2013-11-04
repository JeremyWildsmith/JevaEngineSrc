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
package io.github.jevaengine.world;

import java.util.ArrayList;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Route.SearchNode;
import io.github.jevaengine.world.TraverseRouteTask.IRouteTraveler;

public class WorldNavigationRoutingRules implements IRoutingRules
{

	private IRouteTraveler m_traveler;

	public WorldNavigationRoutingRules(IRouteTraveler traveler)
	{
		m_traveler = traveler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.IRoutingRules#getMovements(jeva.world.Route.SearchNode,
	 * jeva.math.Vector2D)
	 */
	@Override
	public WorldDirection[] getMovements(SearchNode currentNode, @Nullable Vector2D destination) throws IncompleteRouteException
	{

		World world = m_traveler.getWorld();

		ArrayList<WorldDirection> m_directions = new ArrayList<WorldDirection>();

		WorldDirection[] directions = m_traveler.getAllowedMovements();

		for (WorldDirection dir : directions)
		{
			if (world.getTileEffects((currentNode.getLocation(dir))).isTraversable && !currentNode.isIneffective(dir))
			{
				// So sorry for these if statements...
				if (!dir.isDiagonal())
					m_directions.add(dir);
				else if (world.getTileEffects(currentNode.getLocation(WorldDirection.fromVector(new Vector2F(dir.getDirectionVector().x, 0)))).isTraversable && world.getTileEffects(currentNode.getLocation(WorldDirection.fromVector(new Vector2F(0, dir.getDirectionVector().y)))).isTraversable)
					m_directions.add(dir);
			} else if (destination != null && destination.difference(currentNode.getLocation(dir)).isZero())
				throw new IncompleteRouteException(currentNode.getRoute());
		}

		return m_directions.toArray(new WorldDirection[m_directions.size()]);
	}

}
