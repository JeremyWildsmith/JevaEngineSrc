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
