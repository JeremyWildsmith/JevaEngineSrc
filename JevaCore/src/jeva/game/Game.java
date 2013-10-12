package jeva.game;

import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.VolatileImage;

import jeva.IDisposable;
import jeva.graphics.Sprite;
import jeva.graphics.ui.*;
import jeva.joystick.*;
import jeva.joystick.InputManager.InputMouseEvent;
import jeva.math.Vector2D;
import jeva.world.World;

/**
 * The Class Game.
 * 
 */
public abstract class Game implements IInputDeviceListener, IDisposable
{

	/** The m_window manager. */
	private WindowManager m_windowManager;

	/** The m_input man. */
	private InputManager m_inputMan;

	/** The m_world. */
	private World m_world;

	/** The m_cursor. */
	private Sprite m_cursor;

	/** The cursor location. */
	private Vector2D m_cursorLocation = new Vector2D();

	/** The m_render buffer. */
	private VolatileImage m_renderBuffer;

	/** The m_buffer stratedgy. */
	private BufferStrategy m_bufferStratedgy;

	/** The m_gfx config. */
	private GraphicsConfiguration m_gfxConfig;

	/** The m_target width. */
	private int m_targetWidth;

	/** The m_target height. */
	private int m_targetHeight;

	/** The m_render width. */
	private int m_renderWidth;

	/** The m_render height. */
	private int m_renderHeight;

	/**
	 * Instantiates a new game.
	 */
	public Game()
	{
	}

	/**
	 * Inits the.
	 * 
	 * @param target
	 *            the target
	 * @param resolutionX
	 *            the resolution x
	 * @param resolutionY
	 *            the resolution y
	 */
	public final void init(Frame target, int resolutionX, int resolutionY)
	{
		target.createBufferStrategy(2);

		m_renderWidth = resolutionX;
		m_renderHeight = resolutionY;

		m_gfxConfig = target.getGraphicsConfiguration();
		m_targetWidth = target.getWidth();
		m_targetHeight = target.getHeight();

		m_bufferStratedgy = target.getBufferStrategy();

		m_renderBuffer = m_gfxConfig.createCompatibleVolatileImage(m_renderWidth, m_renderHeight);

		m_inputMan = InputManager.create(target);

		m_windowManager = new WindowManager();

		startup();

		m_cursor = getCursor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.IDisposable#dispose()
	 */
	public void dispose()
	{
		if (m_world != null)
			m_world.dispose();
	}

	/**
	 * Gets the world offset.
	 * 
	 * @return the world offset
	 */
	protected Vector2D getWorldOffset()
	{
		return new Vector2D(m_targetWidth / 2, m_targetHeight / 2).difference(getCamera().getLookAt());
	}

	/**
	 * Gets the world scale.
	 * 
	 * @return the world scale
	 */
	protected float getWorldScale()
	{
		return getCamera().getScale();
	}

	/**
	 * Sets the world.
	 * 
	 * @param world
	 *            the new world
	 */
	public final void setWorld(World world)
	{
		m_world = world;
		getCamera().attach(world);
		onLoadedWorld();
	}

	/**
	 * Clear world.
	 */
	public final void clearWorld()
	{
		getCamera().dettach();
		m_world = null;
	}

	/**
	 * Gets the world.
	 * 
	 * @return the world
	 */
	public final World getWorld()
	{
		return m_world;
	}

	/**
	 * Gets the window manager.
	 * 
	 * @return the window manager
	 */
	public final WindowManager getWindowManager()
	{
		return m_windowManager;
	}

	/**
	 * Render.
	 */
	public final void render()
	{
		do
		{
			if (m_renderBuffer.validate(m_gfxConfig) == VolatileImage.IMAGE_INCOMPATIBLE)
			{
				m_renderBuffer.flush();
				m_renderBuffer = m_gfxConfig.createCompatibleVolatileImage(m_renderWidth, m_renderHeight);
			}

			Graphics2D g = (Graphics2D) m_renderBuffer.getGraphics();

			g.setColor(Color.black);
			g.fillRect(0, 0, m_targetWidth, m_targetHeight);

			if (m_world != null)
				m_world.render(g, 1.0F, new Rectangle(getWorldOffset().x, getWorldOffset().y, m_targetWidth, m_targetHeight));

			m_windowManager.render(g, 0, 0, 1.0F);

			m_cursor.render(g, m_cursorLocation.x, m_cursorLocation.y, 1.0F);

			g.dispose();

			g = (Graphics2D) m_bufferStratedgy.getDrawGraphics();
			g.drawImage(m_renderBuffer, 0, 0, m_targetWidth, m_targetHeight, null);
			g.dispose();

			m_bufferStratedgy.show();

		} while (m_bufferStratedgy.contentsLost() || m_renderBuffer.contentsLost());

		Toolkit.getDefaultToolkit().sync();
	}

	/**
	 * Update.
	 * 
	 * @param deltaTime
	 *            the delta time
	 */
	public void update(int deltaTime)
	{
		m_inputMan.process(this);

		m_windowManager.update(deltaTime);
	}

	// Control Input
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseMoved(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseMoved(InputManager.InputMouseEvent e)
	{
		m_cursorLocation = new Vector2D(e.location);

		if (m_windowManager != null)
			m_windowManager.onMouseEvent(e);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseClicked(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseClicked(InputManager.InputMouseEvent e)
	{
		if (m_windowManager != null)
			m_windowManager.onMouseEvent(e);

		if (!e.isConsumed)
		{
			if (m_world != null)
			{
				Vector2D tilePos = m_world.translateScreenToWorld(new Vector2D(e.location.x, e.location.y).difference(getWorldOffset()), getWorldScale());

				if (m_world.getMapBounds().contains(new Point(tilePos.x, tilePos.y)))
					worldSelection(e, tilePos);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.joystick.IInputDeviceListener#keyUp(jeva.joystick.InputManager.
	 * InputKeyEvent)
	 */
	@Override
	public void keyUp(InputManager.InputKeyEvent e)
	{
		if (m_windowManager != null)
			m_windowManager.onKeyEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#keyDown(jeva.joystick.InputManager
	 * .InputKeyEvent)
	 */
	@Override
	public void keyDown(InputManager.InputKeyEvent e)
	{
		if (m_windowManager != null)
			m_windowManager.onKeyEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#keyTyped(jeva.joystick.InputManager
	 * .InputKeyEvent)
	 */
	public void keyTyped(InputManager.InputKeyEvent e)
	{
		if (m_windowManager != null)
			m_windowManager.onKeyEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseWheelMoved(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseWheelMoved(InputManager.InputMouseEvent e)
	{
		if (m_windowManager != null)
			m_windowManager.onMouseEvent(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseLeft(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseLeft(InputManager.InputMouseEvent e)
	{
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * jeva.joystick.IInputDeviceListener#mouseEntered(jeva.joystick.InputManager
	 * .InputMouseEvent)
	 */
	public void mouseEntered(InputManager.InputMouseEvent e)
	{
	}

	/**
	 * Gets the game style.
	 * 
	 * @return the game style
	 */
	public abstract UIStyle getGameStyle();

	/**
	 * Gets the dialog.
	 * 
	 * @return the dialog
	 */
	public abstract DialogMenu getDialog();

	/**
	 * Gets the script bridge.
	 * 
	 * @return the script bridge
	 */
	public abstract IGameScriptProvider getScriptBridge();

	/**
	 * Gets the camera.
	 * 
	 * @return the camera
	 */
	protected abstract IWorldCamera getCamera();

	/**
	 * Startup.
	 */
	protected abstract void startup();

	/**
	 * World selection.
	 * 
	 * @param e
	 *            the e
	 * @param location
	 *            the location
	 */
	protected abstract void worldSelection(InputMouseEvent e, Vector2D location);

	/**
	 * On loaded world.
	 */
	protected abstract void onLoadedWorld();

	/**
	 * Gets the cursor.
	 * 
	 * @return the cursor
	 */
	protected abstract Sprite getCursor();
}
