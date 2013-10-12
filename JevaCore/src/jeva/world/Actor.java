package jeva.world;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.ArrayList;

import javax.script.ScriptException;

import proguard.annotation.KeepClassMemberNames;

import com.sun.istack.internal.Nullable;

import jeva.CoreScriptException;
import jeva.config.ShallowVariable;
import jeva.config.UnknownVariableException;
import jeva.config.Variable;
import jeva.config.VariableValue;
import jeva.graphics.IRenderable;
import jeva.math.Matrix2X2;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.util.StaticSet;
import jeva.world.EffectMap.TileEffects;

/**
 * The Class Actor.
 */
public abstract class Actor extends DialogicalEntity
{

	/** The m_location. */
	private Vector2F m_location;

	/** The m_direction. */
	private WorldDirection m_direction;

	/** The m_observers. */
	private Observers m_observers = new Observers();

	/**
	 * Instantiates a new actor.
	 */
	protected Actor()
	{
		m_location = new Vector2F();
		m_direction = WorldDirection.Zero;
	}

	/**
	 * Instantiates a new actor.
	 * 
	 * @param name
	 *            the name
	 */
	protected Actor(String name)
	{
		super(name);

		m_location = new Vector2F();
		m_direction = WorldDirection.Zero;
	}

	/**
	 * Instantiates a new actor.
	 * 
	 * @param name
	 *            the name
	 * @param direction
	 *            the direction
	 */
	protected Actor(@Nullable String name, WorldDirection direction)
	{
		super(name);

		m_location = new Vector2F();
		m_direction = direction;
	}

	/**
	 * Instantiates a new actor.
	 * 
	 * @param <Y>
	 *            the generic type
	 * @param <T>
	 *            the generic type
	 * @param name
	 *            the name
	 * @param root
	 *            the root
	 * @param entityContext
	 *            the entity context
	 */
	protected <Y extends Actor, T extends ActorBridge<Y>> Actor(@Nullable String name, Variable root, T entityContext)
	{
		super(name, root, entityContext);

		m_location = new Vector2F();
		m_direction = WorldDirection.Zero;
	}

	/**
	 * Adds the observer.
	 * 
	 * @param observer
	 *            the observer
	 */
	public void addObserver(IActorObserver observer)
	{
		m_observers.add(observer);
		super.addObserver(observer);
	}

	/**
	 * Removes the observer.
	 * 
	 * @param observer
	 *            the observer
	 */
	public void removeObserver(IActorObserver observer)
	{
		m_observers.remove(observer);
		super.removeObserver(observer);
	}

	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public final Vector2F getLocation()
	{
		return m_location;
	}

	/**
	 * Sets the location.
	 * 
	 * @param location
	 *            the new location
	 */
	public final void setLocation(Vector2F location)
	{
		Vector2F oldLocation = m_location;
		m_location = location;

		if (!oldLocation.equals(location))
			m_observers.placement(location);
	}

	/**
	 * Move.
	 * 
	 * @param delta
	 *            the delta
	 */
	public final void move(Vector2F delta)
	{
		m_location = m_location.add(delta);

		m_observers.moved(delta);
	}

	/**
	 * Gets the direction.
	 * 
	 * @return the direction
	 */
	public final WorldDirection getDirection()
	{
		return m_direction;
	}

	/**
	 * Sets the direction.
	 * 
	 * @param direction
	 *            the new direction
	 */
	public final void setDirection(WorldDirection direction)
	{
		WorldDirection oldDirection = m_direction;
		m_direction = direction;

		if (!oldDirection.equals(direction))
			m_observers.directionChanged(direction);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#setChild(java.lang.String,
	 * jeva.config.VariableValue)
	 */
	@Override
	public Variable setChild(String name, VariableValue value)
	{
		if (name.compareTo("x") == 0)
		{
			m_location.x = value.getFloat();
		} else if (name.compareTo("y") == 0)
		{
			m_location.y = value.getFloat();
		} else
		{
			throw new UnknownVariableException(name);
		}

		return getChild(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.config.Variable#getChildren()
	 */
	@Override
	protected Variable[] getChildren()
	{
		return new ShallowVariable[]
		{ new ShallowVariable(this, "x", new VariableValue(m_location.x)), new ShallowVariable(this, "y", new VariableValue(m_location.y)) };
	}

	/**
	 * Gets the visible entities.
	 * 
	 * @return the visible entities
	 */
	public Entity[] getVisibleEntities()
	{
		Vector2F viewAt = new Vector2F(this.getDirection().getDirectionVector()).normalize().multiply(getViewDistance());
		Vector2F viewSrcA = viewAt.rotate(-getFieldOfView() / 2.0F).add(this.getLocation());
		Vector2F viewSrcB = viewAt.rotate(getFieldOfView() / 2.0F).add(this.getLocation());

		Actor[] actorsInFov = getWorld().getActors(new TriangleSearchFilter<Actor>(this.getLocation(), viewSrcA, viewSrcB));

		ArrayList<Entity> visibleEntities = new ArrayList<Entity>();

		for (Actor e : actorsInFov)
		{
			if (e != this)
			{
				float fPathSight = TileEffects.merge(getWorld().getTileEffects(new TriangleSearchFilter<TileEffects>(this.getLocation(), e.getLocation(), e.getLocation()))).sightEffect;

				if (getVisualAcuity() >= fPathSight)
					visibleEntities.add(e);
			}
		}

		return visibleEntities.toArray(new Entity[visibleEntities.size()]);

	}

	/**
	 * Enqueue render.
	 */
	protected final void enqueueRender()
	{
		if (!isAssociated())
			throw new WorldAssociationException("Entity is not associated with world and can thus not be rendered.");

		final IRenderable[] renderables = getGraphics();

		if (renderables != null)
		{
			// Optimize for most common case
			if (getTileWidth() == 1 && getTileHeight() == 1)
			{
				for (IRenderable renderable : renderables)
					getWorld().enqueueRender(renderable, new Vector2F(m_location.x, m_location.y));
			} else
			{
				// Start with 1, since we start with 0 on x. If they both
				// started
				// at 0, we would render 0,0 twice.
				for (int y = 1; y < getTileHeight(); y++)
				{
					Vector2D location = new Vector2D(0, y);
					getWorld().enqueueRender(new RenderTile(true, renderables, location), m_location.difference(location));
				}

				for (int x = 0; x < getTileWidth(); x++)
				{
					Vector2D location = new Vector2D(x, 0);
					getWorld().enqueueRender(new RenderTile(false, renderables, location), m_location.difference(location));
				}
			}
		}
	}

	/**
	 * Gets the visibility factor.
	 * 
	 * @return the visibility factor
	 */
	public abstract float getVisibilityFactor();

	/**
	 * Gets the view distance.
	 * 
	 * @return the view distance
	 */
	public abstract float getViewDistance();

	/**
	 * Gets the field of view.
	 * 
	 * @return the field of view
	 */
	public abstract float getFieldOfView();

	/**
	 * Gets the visual acuity.
	 * 
	 * @return the visual acuity
	 */
	public abstract float getVisualAcuity();

	/**
	 * Gets the speed.
	 * 
	 * @return the speed
	 */
	public abstract float getSpeed();

	/**
	 * Gets the tile width.
	 * 
	 * @return the tile width
	 */
	public abstract int getTileWidth();

	/**
	 * Gets the tile height.
	 * 
	 * @return the tile height
	 */
	public abstract int getTileHeight();

	/**
	 * Gets the allowed movements.
	 * 
	 * @return the allowed movements
	 */
	public abstract WorldDirection[] getAllowedMovements();

	/**
	 * Gets the graphics.
	 * 
	 * @return the graphics
	 */
	@Nullable
	public abstract IRenderable[] getGraphics();

	/**
	 * The Class RenderTile.
	 */
	private final class RenderTile implements IRenderable
	{

		/** The m_location. */
		private Vector2D m_location;

		/** The m_renderables. */
		private IRenderable[] m_renderables;

		/** The m_is y axis. */
		private boolean m_isYAxis;

		/**
		 * Instantiates a new render tile.
		 * 
		 * @param isYAxis
		 *            the is y axis
		 * @param renderables
		 *            the renderables
		 * @param location
		 *            the location
		 */
		public RenderTile(boolean isYAxis, IRenderable[] renderables, Vector2D location)
		{
			m_isYAxis = isYAxis;
			m_renderables = renderables;
			m_location = location;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.graphics.IRenderable#render(java.awt.Graphics2D, int, int,
		 * float)
		 */
		@Override
		public void render(Graphics2D g, int x, int y, float fScale)
		{
			Matrix2X2 worldMat = getWorld().getPerspectiveMatrix(fScale);

			// Get tile width...
			Vector2F v = worldMat.dot(new Vector2F(1, -1));

			Vector2F renderv = worldMat.dot(new Vector2F(m_location.x, m_location.y));

			if (x - (m_isYAxis ? 0 : v.x / 2) + (m_isYAxis ? v.x / 2 : v.x) > 0)
			{
				Shape lastClip = g.getClip();

				g.clipRect(x - (m_isYAxis ? 0 : (int) v.x / 2), 0, (int) (m_isYAxis ? v.x / 2 : v.x), Integer.MAX_VALUE);

				for (IRenderable r : m_renderables)
					r.render(g, (int) (x + renderv.x), (int) (y + renderv.y), fScale);

				g.setClip(lastClip);
			}
		}
	}

	/**
	 * An asynchronous update interface for receiving notifications about IActor
	 * information as the IActor is constructed.
	 */
	public interface IActorObserver extends IDialogueObserver
	{

		/**
		 * This method is called when information about an IActor which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 * 
		 * @param direction
		 *            the direction
		 */
		void directionChanged(WorldDirection direction);

		/**
		 * This method is called when information about an IActor which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 * 
		 * @param location
		 *            the location
		 */
		void placement(Vector2F location);

		/**
		 * This method is called when information about an IActor which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 * 
		 * @param delta
		 *            the delta
		 */
		void moved(Vector2F delta);
	}

	/**
	 * The Class Observers.
	 */
	private static class Observers extends StaticSet<IActorObserver>
	{

		/**
		 * Placement.
		 * 
		 * @param location
		 *            the location
		 */
		public void placement(Vector2F location)
		{
			for (IActorObserver observer : this)
				observer.placement(location);
		}

		/**
		 * Direction changed.
		 * 
		 * @param direction
		 *            the direction
		 */
		public void directionChanged(WorldDirection direction)
		{
			for (IActorObserver observer : this)
				observer.directionChanged(direction);
		}

		/**
		 * Moved.
		 * 
		 * @param delta
		 *            the delta
		 */
		public void moved(Vector2F delta)
		{
			for (IActorObserver observer : this)
				observer.moved(delta);
		}
	}

	/**
	 * The Class ActorBridge.
	 * 
	 * @param <Y>
	 *            the generic type
	 */
	@KeepClassMemberNames
	public static class ActorBridge<Y extends Actor> extends DialogicalBridge<Y>
	{

		/**
		 * The Class FoundDetails.
		 */
		@KeepClassMemberNames
		public static class FoundDetails
		{

			/** The name. */
			public String name;

			/** The target. */
			public ActorBridge<?> target;

			/** The location. */
			public Vector2D location;

			/**
			 * Instantiates a new found details.
			 * 
			 * @param foundEntity
			 *            the found entity
			 */
			public FoundDetails(Actor foundEntity)
			{
				name = foundEntity.getName();
				location = foundEntity.getLocation().round();
				target = (ActorBridge<?>) foundEntity.getScriptBridge();
			}
		}

		/**
		 * Distance.
		 * 
		 * @param actor
		 *            the actor
		 * @return the float
		 */
		public float distance(ActorBridge<?> actor)
		{
			return getMe().getLocation().difference(actor.getLocation()).getLength();
		}

		/**
		 * Gets the location.
		 * 
		 * @return the location
		 */
		public Vector2D getLocation()
		{
			return getMe().getLocation().round();
		}

		/**
		 * Sets the location.
		 * 
		 * @param location
		 *            the new location
		 */
		public void setLocation(Vector2F location)
		{
			getMe().setLocation(location);
		}

		/**
		 * Sets the location.
		 * 
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 */
		public void setLocation(float x, float y)
		{
			getMe().setLocation(new Vector2F(x, y));
		}

		/**
		 * Look.
		 */
		public void look()
		{
			getMe().addTask(new SearchForTask<Actor>(getMe(), Actor.class)
			{
				@Override
				public boolean found(Actor actor)
				{
					try
					{
						return ((Boolean) getMe().getScript().invokeScriptFunction("lookFound", new FoundDetails(actor))).booleanValue();

					} catch (ScriptException e)
					{
						throw new CoreScriptException("Error occured while attempting to invoke script found routine: " + e.getMessage());
					} catch (NoSuchMethodException e)
					{
						return true;
					}
				}

				@Override
				public void nothingFound()
				{
				}

				@Override
				public boolean continueSearch()
				{
					return false;
				}
			});
		}
	}
}
