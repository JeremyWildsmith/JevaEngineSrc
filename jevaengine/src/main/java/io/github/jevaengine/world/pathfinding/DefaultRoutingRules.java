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
package io.github.jevaengine.world.pathfinding;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.World;

import java.util.ArrayList;

public class DefaultRoutingRules implements IRoutingRules
{
	private Direction[] m_allowedMovements;
	
	public DefaultRoutingRules(Direction[] allowedMovements)
	{
		m_allowedMovements = allowedMovements;
	}

	@Override
	public Direction[] getMovements(World world, SearchNode currentNode, @Nullable Vector2D destination)
	{
		ArrayList<Direction> m_directions = new ArrayList<Direction>();

		for (Direction dir : m_allowedMovements)
		{
			if (world.getTileEffects((currentNode.getLocation(dir))).isTraversable() && !currentNode.isIneffective(dir))
			{
				// So sorry for these if statements...
				if (!dir.isDiagonal())
					m_directions.add(dir);
				else if (world.getTileEffects(currentNode.getLocation(Direction.fromVector(new Vector2F(dir.getDirectionVector().x, 0)))).isTraversable() &&
							world.getTileEffects(currentNode.getLocation(Direction.fromVector(new Vector2F(0, dir.getDirectionVector().y)))).isTraversable())
					m_directions.add(dir);
			} else if (destination != null && destination.difference(currentNode.getLocation(dir)).isZero())
				return new Direction[0]; //Nowhere to go to arrive at destination...
		}

		return m_directions.toArray(new Direction[m_directions.size()]);
	}

}
