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
package io.github.jevaengine.world;

import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.EffectMap.TileEffects;

import java.util.ArrayList;

import javax.script.ScriptException;

import org.mozilla.javascript.NativeArray;

public abstract class Actor extends Entity implements IInteractable
{
	private final ActorScript m_script = new ActorScript();

	private boolean m_isInteractable = false;
	
	public Actor()
	{
		this(null);
	}
	
	public Actor(String name)
	{
		super(name);
	}
	
	protected <Y extends Actor, T extends ActorBridge<Y>> Actor(@Nullable String name, IImmutableVariable root, T entityContext)
	{
		super(name, root, entityContext);
		
		if(root.childExists("isInteractable"))
			m_isInteractable = root.getChild("isInteractable").getValue(Boolean.class);
	
	}

	@Override
	@Nullable
	public String getDefaultCommand()
	{
		return m_script.getDefaultCommand();
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

	@Override
	public void blendEffectMap(EffectMap globalEffectMap)
	{
		if (m_isInteractable)
		{
			globalEffectMap.applyOverlayEffects(this.getLocation().round(),
					new TileEffects(this));
		}
	}

	public Entity[] getVisibleEntities()
	{
		Actor[] actorsInFov = getWorld().getActors(new RadialSearchFilter<Actor>(getLocation(), getFieldOfView()));

		ArrayList<Entity> visibleEntities = new ArrayList<Entity>();

		float viewAngle = getDirection().getDirectionVector().getAngle();
		
		for (Actor e : actorsInFov)
		{
			if (e == this)
				continue;
			
			float differenceAngle = Math.abs(viewAngle - e.getLocation().difference(getLocation()).getAngle());
			
			float pathSight = TileEffects.merge(getWorld().getTileEffects(new TriangleSearchFilter<TileEffects>(this.getLocation(), e.getLocation(), e.getLocation()))).sightEffect;

			if (getVisualAcuity() >= pathSight && differenceAngle <= getFieldOfView() / 2)
				visibleEntities.add(e);
		}

		return visibleEntities.toArray(new Entity[visibleEntities.size()]);

	}

	protected final void enqueueRender()
	{
		if (!isAssociated())
			throw new WorldAssociationException("Entity is not associated with world and can thus not be rendered.");

		final IRenderable renderable = getGraphic();
		
		if (renderable != null)
		{
			getWorld().enqueueRender(renderable, Actor.this, getLocation());
		}
	}
	
	@Nullable
	public boolean testPick(int x, int y, float scale)
	{
		return false;
	}

	public float getVisibilityFactor() { return 0; }
	public float getViewDistance() { return 0; }
	public float getFieldOfView() { return 0; }
	public float getVisualAcuity() { return 0; }
	public float getSpeed() { return 0; }
	public WorldDirection[] getAllowedMovements() { return new WorldDirection[0]; }
	
	@Nullable
	public abstract IRenderable getGraphic();

	public abstract int getTileWidth();
	public abstract int getTileHeight();

	private class ActorScript
	{
		@Nullable
		public String getDefaultCommand()
		{
			try
			{
				Object jsString = getScript().invokeScriptFunction("getDefaultCommand");
				
				if(!(jsString instanceof String) && jsString != null)
					throw new CoreScriptException("Unexpected return value from getDefaultCommand script routine.");
				
				return (String)jsString;
			} catch (NoSuchMethodException e)
			{
				return null;
			} catch (ScriptException e)
			{
				throw new CoreScriptException(e);
			}
		}
		
		public String[] getCommands()
		{
			try
			{
				NativeArray jsStringArray = (NativeArray) getScript().invokeScriptFunction("getCommands");

				if(jsStringArray == null)
					return new String[0];
				
				String[] commands = new String[(int) jsStringArray.getLength()];

				for (int i = 0; i < commands.length; i++)
				{
					Object element = jsStringArray.get(i, null);

					if (!(element instanceof String))
						throw new CoreScriptException("Unexpected data returned on invoking getCommands for Actor Interactable entity.");

					commands[i] = (String) element;
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
	
	public static class ActorBridge<Y extends Actor> extends EntityBridge<Y>
	{
		private SearchForTask<?> m_searchTask;

		public void beginLook()
		{
			if(m_searchTask == null)
				m_searchTask = new ActorSearch(getEntity());
			
			if(!getEntity().isTaskActive(m_searchTask))
				getEntity().addTask(m_searchTask);
		}
		
		public void endLook()
		{
			if(m_searchTask != null && getEntity().isTaskActive(m_searchTask))
				getEntity().cancelTask(m_searchTask);
		}

		private class ActorSearch extends SearchForTask<Actor>
		{
			public ActorSearch(Actor me)
			{
				super(me, Actor.class);
			}
			@Override
			public boolean found(Actor actor)
			{
				try 
				{
					Object result = getEntity().getScript().invokeScriptFunction("lookFound", new FoundDetails(actor));
					
					if(!(result instanceof Boolean))
						throw new CoreScriptException("Unexpected return from lookFound method.");
					
					return ((Boolean)result).booleanValue();

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
				return true;
			}
		}
		
		public static class FoundDetails
		{
			public String name;

			public ActorBridge<?> target;

			public Vector2D location;

			public FoundDetails(Actor foundEntity)
			{
				name = foundEntity.getInstanceName();
				location = foundEntity.getLocation().round();
				target = (ActorBridge<?>) foundEntity.getScriptBridge();
			}
		}
	}
}
