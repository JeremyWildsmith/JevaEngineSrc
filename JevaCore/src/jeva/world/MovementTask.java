package jeva.world;

import jeva.math.Vector2F;

import com.sun.istack.internal.Nullable;

/**
 * The Class MovementTask.
 */
public abstract class MovementTask implements ITask
{
	/** The m_traveler. */
	private ITraveler m_traveler;

	/** The m_query cancel. */
	private boolean m_queryCancel;

	/** The m_last destination. */
	private Vector2F m_lastDestination = null;

	/**
	 * Instantiates a new movement task.
	 * 
	 * @param traveler
	 *            the traveler
	 */
	public MovementTask(ITraveler traveler)
	{
		m_queryCancel = false;

		m_traveler = traveler;
	}

	/**
	 * Gets the traveler.
	 * 
	 * @return the traveler
	 */
	protected ITraveler getTraveler()
	{
		return m_traveler;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#begin(jeva.world.Entity)
	 */
	@Override
	public void begin(Entity entity)
	{
		m_queryCancel = false;
		m_lastDestination = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#end()
	 */
	public void end()
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#doCycle(int)
	 */
	public final boolean doCycle(int deltaTime)
	{
		if (m_queryCancel)
		{
			m_queryCancel = false;
			m_traveler.updateMovement(new Vector2F());
			return true;
		}

		if (m_lastDestination == null || !m_lastDestination.equals(getDestination()))
		{
			m_lastDestination = new Vector2F(getDestination());
			m_traveler.setDestination(m_lastDestination);
		}

		Vector2F deltaMovement = getDestination().difference(m_traveler.getLocation());
		Vector2F advancement = (deltaMovement.isZero() ? new Vector2F() : deltaMovement.normalize().multiply(m_traveler.getSpeed() * ((float) deltaTime / 1000)));

		deltaMovement = (deltaMovement.getLengthSquared() < advancement.getLengthSquared() ? deltaMovement : advancement);

		RouteNode lastNode = m_traveler.getWorld().getRouteNode(m_traveler.getLocation().round());
		RouteNode nextNode = m_traveler.getWorld().getRouteNode(m_traveler.getLocation().add(deltaMovement).round());

		// If we're about to move on to target node.
		if (lastNode != nextNode)
		{
			// Try to take control of it
			if (nextNode.isTraversable() && nextNode.take())
			{
				lastNode.release();
			} else
			{
				m_traveler.updateMovement(new Vector2F());

				blocking();

				return m_queryCancel;
			}
		}// If we've just arrived at our target
		else if (deltaMovement.isZero())
		{
			if (atDestination())
			{
				m_traveler.updateMovement(deltaMovement);
				nextNode.release();
				return true;
			} else
				return false;
		}

		m_traveler.updateMovement(deltaMovement);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#cancel()
	 */
	@Override
	public void cancel()
	{
		m_traveler.getWorld().getRouteNode(m_traveler.getLocation().round()).release();
		m_queryCancel = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#isParallel()
	 */
	@Override
	public final boolean isParallel()
	{
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ITask#ignoresPause()
	 */
	@Override
	public final boolean ignoresPause()
	{
		return false;
	}

	/**
	 * Blocking.
	 */
	protected abstract void blocking();

	/**
	 * Gets the destination.
	 * 
	 * @return the destination
	 */
	@Nullable
	protected abstract Vector2F getDestination();

	/**
	 * At destination.
	 * 
	 * @return true, if successful
	 */
	protected abstract boolean atDestination();

	/**
	 * Checks for next.
	 * 
	 * @return true, if successful
	 */
	protected abstract boolean hasNext();

	/**
	 * The Interface ITraveler.
	 */
	public interface ITraveler
	{
		/**
		 * Update movement.
		 * 
		 * @param delta
		 *            the delta
		 */
		void updateMovement(Vector2F delta);

		/**
		 * Sets the destination.
		 * 
		 * @param target
		 *            the new destination
		 */
		void setDestination(Vector2F target);

		/**
		 * Gets the world.
		 * 
		 * @return the world
		 */
		World getWorld();

		/**
		 * Gets the location.
		 * 
		 * @return the location
		 */
		Vector2F getLocation();

		/**
		 * Gets the speed.
		 * 
		 * @return the speed
		 */
		float getSpeed();
	}
}
