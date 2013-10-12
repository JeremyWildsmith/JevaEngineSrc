package jeva.game;

import com.sun.istack.internal.Nullable;

import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.World;

/**
 * The Class ControlledCamera.
 */
public final class ControlledCamera implements IWorldCamera
{

	/** The m_world. */
	@Nullable private World m_world;

	/** Tile Offset */
	private Vector2F m_lookAtTile;
	
	/** Screen Offset */
	private Vector2D m_lookAtScreen;

	/**
	 * Instantiates a new controlled camera.
	 */
	public ControlledCamera()
	{
		m_lookAtTile = new Vector2F();
		m_lookAtScreen = new Vector2D();
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
		if(m_world != null)
		{
			float fX = Math.min(Math.max(0, tileLocation.x), m_world.getWidth() - 1);
			float fY = Math.min(Math.max(0, tileLocation.y), m_world.getHeight() - 1);
			m_lookAtTile = new Vector2F(fX, fY);
			
			m_lookAtScreen = m_world.translateWorldToScreen(m_lookAtTile, getScale());
		}else
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
		lookAt(m_lookAtTile.add(delta));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.game.IWorldCamera#getLookAt()
	 */
	@Override
	public Vector2D getLookAt()
	{
		return m_lookAtScreen;
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
		
		//Refresh target tile with current world
		lookAt(m_lookAtTile);
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
