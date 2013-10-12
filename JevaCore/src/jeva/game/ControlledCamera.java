package jeva.game;

import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.World;

/**
 * The Class ControlledCamera.
 */
public final class ControlledCamera implements IWorldCamera
{

	/** The m_world. */
	private World m_world;

	/** The m_look at tile. */
	private Vector2F m_lookAtTile;

	/**
	 * Instantiates a new controlled camera.
	 */
	public ControlledCamera()
	{
		m_lookAtTile = new Vector2F();
	}

	/**
	 * Instantiates a new controlled camera.
	 * 
	 * @param tileLocation
	 *            the tile location
	 */
	public ControlledCamera(Vector2F tileLocation)
	{
		lookAt(tileLocation);
	}

	/**
	 * Look at.
	 * 
	 * @param tileLocation
	 *            the tile location
	 */
	public void lookAt(Vector2F tileLocation)
	{
		m_lookAtTile = tileLocation;
	}

	/**
	 * Move.
	 * 
	 * @param delta
	 *            the delta
	 */
	public void move(Vector2F delta)
	{
		m_lookAtTile = m_lookAtTile.add(delta);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#getLookAt()
	 */
	@Override
	public Vector2D getLookAt()
	{
		if (m_world == null)
			return new Vector2D();

		float fX = Math.min(Math.max(0, m_lookAtTile.x), m_world.getWidth() - 1);
		float fY = Math.min(Math.max(0, m_lookAtTile.y), m_world.getHeight() - 1);
		m_lookAtTile = new Vector2F(fX, fY);

		return m_world.translateWorldToScreen(m_lookAtTile, getScale());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#attach(jeva.world.World)
	 */
	@Override
	public void attach(World world)
	{
		m_world = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#dettach()
	 */
	@Override
	public void dettach()
	{
		m_world = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#getScale()
	 */
	@Override
	public float getScale()
	{
		return 1.0F;
	}
}
