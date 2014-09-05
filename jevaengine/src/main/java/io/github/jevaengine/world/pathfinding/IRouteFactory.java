package io.github.jevaengine.world.pathfinding;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.World;

public interface IRouteFactory
{
	Route create(IRoutingRules routingRules, World world, Vector2F startPoint, Vector2F endPoint, float arrivalTolorance) throws IncompleteRouteException;
	Route create(IRoutingRules routingRules, World world, Vector2F startPoint, int length);
}
