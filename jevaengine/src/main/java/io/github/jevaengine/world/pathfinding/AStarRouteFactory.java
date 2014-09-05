package io.github.jevaengine.world.pathfinding;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.World;

import java.util.ArrayList;
import java.util.Random;

public final class AStarRouteFactory implements IRouteFactory
{
	private static final int MAX_PATH_ITERATIONS = 150;
	private static final float PATHING_CONSISTANCY_PROBABILITY = 0.7F;

	@Override
	public Route create(IRoutingRules routingRules, World world, Vector2F startPoint, Vector2F endPoint, float arrivalTolorance) throws IncompleteRouteException
	{
		Route route = new Route();

		//Handle corner-cases, where endpoint cannot be reached or where routing to start point 
		if(!world.getTileEffects(endPoint.round()).isTraversable() && arrivalTolorance <= 0)
			throw new IncompleteRouteException(route);
		
		ArrayList<SearchNode> open = new ArrayList<SearchNode>();
		ArrayList<SearchNode> closed = new ArrayList<SearchNode>();

		SearchNode base = new SearchNode(world, null, Direction.Zero, startPoint.round());
		open.add(base);

		SearchNode best = null;

		for (int iterations = 0; iterations < MAX_PATH_ITERATIONS && !open.isEmpty(); iterations++)
		{
			best = open.get(0);
			
			for (SearchNode node : open)
			{
				if (node.getCost(endPoint.round()) < best.getCost(endPoint.round()))
					best = node;
			}

			if (endPoint.round().difference(best.getLocation()).getLength() <= arrivalTolorance)
			{
				ArrayList<Vector2F> bestRoute = best.traverseRoute();
				bestRoute.remove(0); //Remove first node (Start point) since this is a path from start to end. Assuming we are already at the start.
				
				route.addWaypoints(bestRoute.toArray(new Vector2F[bestRoute.size()]));
				
				return route;
			} else
			{
				open.remove(best);
				closed.add(best);

				for (Direction dir : routingRules.getMovements(world, best, endPoint.round()))
				{
					SearchNode step = best.addNode(dir);

					if (!open.contains(step) && !closed.contains(step))
						open.add(step);
				}
			}
		}

		if (best != null)
		{
			ArrayList<Vector2F> bestRoute = best.traverseRoute();
			bestRoute.remove(0); //Remove first node (Start point) since this is a path from start to end. Assuming we are already at the start.
			
			route.addWaypoints(bestRoute.toArray(new Vector2F[bestRoute.size()]));
			throw new IncompleteRouteException(route);
		} else
			throw new IncompleteRouteException(new Route());
	}

	@Override
	public Route create(IRoutingRules routingRules, World world, Vector2F startPoint, int length)
	{
		Route route = new Route();

		SearchNode tail = new SearchNode(world, null, Direction.Zero, startPoint.round());
		SearchNode head = tail;

		Random random = new Random();

		Direction lastDirection = Direction.Zero;

		while (head.traverseRoute().size() < length)
		{
			for (Direction dir : routingRules.getMovements(world, head, null))
				head.addNode(dir);

			if (head.getChildren().length <= 0)
				break;
			else
			{
				SearchNode consistantNode = lastDirection == Direction.Zero ? null : head.getChildNode(lastDirection);

				if (consistantNode == null || random.nextFloat() > PATHING_CONSISTANCY_PROBABILITY)
					head = head.getChildren()[random.nextInt(head.getChildren().length)];
				else
					head = consistantNode;

				lastDirection = head.getDirection();
			}
		}

		ArrayList<Vector2F> routeNodes = head.traverseRoute();
		route.addWaypoints(routeNodes.toArray(new Vector2F[routeNodes.size()]));

		return route;

	}

}
