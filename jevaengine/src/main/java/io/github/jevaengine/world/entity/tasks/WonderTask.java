package io.github.jevaengine.world.entity.tasks;

import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.pathfinding.IRouteFactory;
import io.github.jevaengine.world.pathfinding.IRoutingRules;
import io.github.jevaengine.world.pathfinding.Route;
import io.github.jevaengine.world.steering.ISteeringDriverFactory;

public final class WonderTask implements ITask
{
	private final TraverseRouteTask m_traverseRouteTask;
	private final IRouteFactory m_routeFactory;
	private final IRoutingRules m_routingRules;
	
	private final int m_wonderRadius;
	
	public WonderTask(ISteeringDriverFactory driverFactory, IRouteFactory routeFactory, IRoutingRules routingRules, int wonderRadius)
	{
		m_traverseRouteTask = new TraverseRouteTask(driverFactory);
		m_routeFactory = routeFactory;
		m_routingRules = routingRules;
		m_wonderRadius = wonderRadius;
	}
	
	@Override
	public void begin(IEntity entity)
	{
		Route route = m_routeFactory.create(m_routingRules, entity.getWorld(), entity.getBody().getLocation().getXy(), m_wonderRadius);

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
