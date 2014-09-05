package io.github.jevaengine.world.entity.tasks;

import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.pathfinding.IRouteFactory;
import io.github.jevaengine.world.pathfinding.IRoutingRules;
import io.github.jevaengine.world.pathfinding.IncompleteRouteException;
import io.github.jevaengine.world.pathfinding.Route;
import io.github.jevaengine.world.steering.ISteeringDriverFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MovementTask implements ITask
{
	private final Logger m_logger = LoggerFactory.getLogger(MovementTask.class);
	
	private final TraverseRouteTask m_traverseRouteTask;
	private final IRouteFactory m_routeFactory;
	private final IRoutingRules m_routingRules;
	
	private final Vector2F m_destination;
	private final float m_arrivalTolorance;
	
	public MovementTask(ISteeringDriverFactory driverFactory, IRouteFactory routeFactory, IRoutingRules routingRules, Vector2F destination, float arrivalTolorance)
	{
		m_traverseRouteTask = new TraverseRouteTask(driverFactory);
		m_routeFactory = routeFactory;
		m_routingRules = routingRules;
		m_destination = new Vector2F(destination);
		m_arrivalTolorance = arrivalTolorance;
	}
	
	@Override
	public void begin(IEntity entity)
	{
		Route route = new Route();
		
		try
		{
			route = m_routeFactory.create(m_routingRules, entity.getWorld(), entity.getBody().getLocation().getXy(), m_destination, m_arrivalTolorance);
		} catch (IncompleteRouteException e) {
			m_logger.error(String.format("Unable to constuct path to %f, %f for entity %s.", m_destination.x, m_destination.y, entity.getInstanceName()));
		}
		
		m_traverseRouteTask.setRoute(route);
		
		m_traverseRouteTask.begin(entity);
	}

	@Override
	public void end()
	{
		m_traverseRouteTask.end();
	}

	@Override
	public void cancel()
	{
		m_traverseRouteTask.cancel();
	}

	@Override
	public boolean doCycle(int deltaTime)
	{
		return m_traverseRouteTask.doCycle(deltaTime);
	}

	@Override
	public boolean isParallel()
	{
		return m_traverseRouteTask.isParallel();
	}
}
