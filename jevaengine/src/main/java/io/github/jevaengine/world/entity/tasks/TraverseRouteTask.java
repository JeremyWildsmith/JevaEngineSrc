package io.github.jevaengine.world.entity.tasks;

import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.pathfinding.Route;
import io.github.jevaengine.world.steering.AvoidanceBehavior;
import io.github.jevaengine.world.steering.ISteeringDriver;
import io.github.jevaengine.world.steering.ISteeringDriverFactory;
import io.github.jevaengine.world.steering.TraverseRouteBehavior;

public final class TraverseRouteTask implements ITask
{
	private final float ARRIVAL_TOLORANCE = 0.1F;
	
	private final ISteeringDriver m_driver;
	private Route m_route = new Route();
	
	public TraverseRouteTask(ISteeringDriverFactory driverFactory)
	{
		m_driver = driverFactory.create();
	}
	
	public void setRoute(Route route)
	{
		m_route = new Route(route);
	}
	
	@Override
	public void begin(IEntity entity)
	{
		m_driver.attach(entity.getBody());
		m_driver.clear();
		m_driver.add(new TraverseRouteBehavior(1.0F, m_route, ARRIVAL_TOLORANCE));
		m_driver.add(new AvoidanceBehavior(0.3F));
	}

	@Override
	public void end()
	{
		m_driver.dettach();
	}

	@Override
	public void cancel()
	{
		m_driver.clear();
	}

	@Override
	public boolean doCycle(int deltaTime)
	{
		m_driver.update(deltaTime);
		return !m_driver.isDriving();
	}

	@Override
	public boolean isParallel()
	{
		return false;
	}

}
