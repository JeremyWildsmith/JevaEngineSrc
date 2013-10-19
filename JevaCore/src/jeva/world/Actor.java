/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package jeva.world;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.ArrayList;

import javax.script.ScriptException;

import proguard.annotation.KeepClassMemberNames;
import sun.org.mozilla.javascript.internal.NativeArray;

import com.sun.istack.internal.Nullable;

import jeva.Core;
import jeva.CoreScriptException;
import jeva.IResourceLibrary;
import jeva.config.BasicVariable;
import jeva.config.ShallowVariable;
import jeva.config.UnknownVariableException;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.config.VariableValue;
import jeva.game.DialogPath;
import jeva.game.DialogPath.Query;
import jeva.graphics.IRenderable;
import jeva.math.Matrix2X2;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.util.StaticSet;
import jeva.world.EffectMap.TileEffects;


public abstract class Actor extends Entity implements IInteractable
{

	/** The m_location. */
	private Vector2F m_location;

	/** The m_direction. */
	private WorldDirection m_direction;

	/** The m_observers. */
	private Observers m_observers = new Observers();
	
	private ActorScript m_script = new ActorScript();

	private boolean m_isInteractable;
	
	@Nullable
	private DialogPath m_dialog;
	
	
	protected Actor()
	{
		this(null);
	}

	
	protected Actor(String name)
	{
		this(name, WorldDirection.Zero);
	}

	
	protected Actor(@Nullable String name, WorldDirection direction)
	{
		this(name, new BasicVariable(), new ActorBridge<>());
		m_direction = direction;
	}

	
	protected <Y extends Actor, T extends ActorBridge<Y>> Actor(@Nullable String name, Variable root, T entityContext)
	{
		super(name, root, entityContext);

		m_location = new Vector2F();
		m_direction = WorldDirection.Zero;
		
		Variable entityVar = getEntityVariables();
		
		if(entityVar.variableExists("isInteractable"))
			m_isInteractable = entityVar.getVariable("isInteractable").getValue().getBoolean();
		else
			m_isInteractable = false;
		
		if (entityVar.variableExists("dialog"))
			m_dialog = DialogPath.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(root.getVariable("dialog").getValue().getString())));
	}

	
	public void addObserver(IActorObserver observer)
	{
		m_observers.add(observer);
		super.addObserver(observer);
	}

	
	public void removeObserver(IActorObserver observer)
	{
		m_observers.remove(observer);
		super.removeObserver(observer);
	}
	
	public final int invokeDialogEvent(@Nullable Entity subject, int event)
	{
		try
		{
			m_observers.onDialogEvent(subject, event);

			Object oReturn = getScript().invokeScriptFunction("onDialogEvent", subject, event);

			if (oReturn instanceof Double)
				return ((Double) oReturn).intValue();
			else if (oReturn instanceof Integer)
				return ((Integer) oReturn).intValue();
			else
				return -1;
		} catch (ScriptException e)
		{
			throw new CoreScriptException("Unable to invoke entity onDialogEvent routine." + e.getMessage());
		} catch (NoSuchMethodException e)
		{
			return -1;
		}
	}
	
	
	public final Vector2F getLocation()
	{
		return m_location;
	}

	
	public final void setLocation(Vector2F location)
	{
		Vector2F oldLocation = m_location;
		m_location = location;

		if (!oldLocation.equals(location))
			m_observers.placement(location);
	}

	
	public final void move(Vector2F delta)
	{
		m_location = m_location.add(delta);

		m_observers.moved(delta);
	}

	
	public final WorldDirection getDirection()
	{
		return m_direction;
	}

	
	public final void setDirection(WorldDirection direction)
	{
		WorldDirection oldDirection = m_direction;
		m_direction = direction;

		if (!oldDirection.equals(direction))
			m_observers.directionChanged(direction);
	}

	@Override
	public String[] getCommands()
	{
		return m_script.getCommands();
	}

	@Override
	public void doCommand(String command)
	{
		m_script.doCommand(command);
	}
	

	public void blendEffectMap(EffectMap globalEffectMap)
	{
		if(m_isInteractable)
		{
			globalEffectMap.applyOverlayEffects(this.getLocation().round(),
											 		new TileEffects(this));
		}
	}
	
	
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


	
	public abstract float getVisibilityFactor();

	public abstract float getViewDistance();

	public abstract float getFieldOfView();

	
	public abstract float getVisualAcuity();

	
	public abstract float getSpeed();

	
	public abstract int getTileWidth();

	
	public abstract int getTileHeight();

	public abstract WorldDirection[] getAllowedMovements();

	@Nullable
	public abstract IRenderable[] getGraphics();

	private final class RenderTile implements IRenderable
	{

		private Vector2D m_location;

		private IRenderable[] m_renderables;

		private boolean m_isYAxis;

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

	public interface IActorObserver extends IEntityObserver
	{

		void directionChanged(WorldDirection direction);

		void placement(Vector2F location);

		void moved(Vector2F delta);
		
		void onDialogEvent(Entity subject, int event);
	}

	
	private static class Observers extends StaticSet<IActorObserver>
	{

		
		public void placement(Vector2F location)
		{
			for (IActorObserver observer : this)
				observer.placement(location);
		}

		
		public void directionChanged(WorldDirection direction)
		{
			for (IActorObserver observer : this)
				observer.directionChanged(direction);
		}

		
		public void moved(Vector2F delta)
		{
			for (IActorObserver observer : this)
				observer.moved(delta);
		}
		

		public void onDialogEvent(Entity subject, int event)
		{
			for (IActorObserver observer : this)
				observer.onDialogEvent(subject, event);
		}
	}

	private class ActorScript
	{
		public String[] getCommands()
		{
			try
			{
				NativeArray jsStringArray = (NativeArray)getScript().invokeScriptFunction("getCommands");
				
				String[] commands = new String[(int) jsStringArray.getLength()];
				
				for(int i = 0; i < jsStringArray.getLength(); i++)
				{
					Object element = jsStringArray.get(i);
					
					if(!(element instanceof String))
						throw new CoreScriptException("Unexpected data returned on invoking getCommands for Actor Interactable entity.");

					commands[i] = (String)element;
				}
				
				return commands;
				
			} catch (NoSuchMethodException e)
			{
				return new String[0];
			} catch (ScriptException e)
			{
				throw new CoreScriptException(e);
			}
		}
		
		public void doCommand(String command)
		{
			try
			{
				getScript().invokeScriptFunction("doCommand", command);
			} catch (NoSuchMethodException e)
			{
			} catch (ScriptException e)
			{
				throw new CoreScriptException(e);
			}
		}
	}
	
	
	@KeepClassMemberNames
	public static class ActorBridge<Y extends Actor> extends EntityBridge<Y>
	{

		
		@KeepClassMemberNames
		public static class FoundDetails
		{

			/** The name. */
			public String name;

			/** The target. */
			public ActorBridge<?> target;

			/** The location. */
			public Vector2D location;

			
			public FoundDetails(Actor foundEntity)
			{
				name = foundEntity.getName();
				location = foundEntity.getLocation().round();
				target = (ActorBridge<?>) foundEntity.getScriptBridge();
			}
		}

		
		public float distance(ActorBridge<?> actor)
		{
			return getMe().getLocation().difference(actor.getLocation()).getLength();
		}

		
		public Vector2D getLocation()
		{
			return getMe().getLocation().round();
		}

		
		public void setLocation(Vector2F location)
		{
			getMe().setLocation(location);
		}

		
		public void setLocation(float x, float y)
		{
			getMe().setLocation(new Vector2F(x, y));
		}

		
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
		
		public final void dialog(@Nullable final EntityBridge<Entity> subject, final int dialogId)
		{
			final DialogPath dialog = ((Actor)getMe()).m_dialog;

			if (dialog == null)
				throw new CoreScriptException("Entity attempting to initiate dialog without defining a dialog path.");

			getMe().addTask(new DialogTask(subject.getMe())
			{
				@Override
				public Query onEvent(Entity subject, int eventCode)
				{
					int dialogOverride = getMe().invokeDialogEvent(subject, eventCode);

					if (dialogOverride >= 0)
						return dialog.getQuery(dialogOverride);
					else
						return null;
				}

				@Override
				public void onDialogEnd()
				{
				}

				@Override
				public Query getEntryDialog()
				{
					return dialog.getQuery(dialogId);
				}
			});
		}
	}
}
