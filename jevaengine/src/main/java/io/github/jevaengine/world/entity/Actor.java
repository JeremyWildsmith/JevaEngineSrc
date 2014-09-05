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
package io.github.jevaengine.world.entity;

import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.audio.NullAudioClipFactory;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.script.ArrayScriptDelegate;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.NullFunctionFactory;
import io.github.jevaengine.script.ScriptDelegate;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.entity.tasks.SearchForTask;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;
import io.github.jevaengine.world.search.RadialSearchFilter;
import io.github.jevaengine.world.search.TriangleSearchFilter;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Actor extends DefaultEntity implements IInteractable
{
	private final ActorBridgeNotifier m_script = new ActorBridgeNotifier();
	private ActorBridge m_bridge;

	private final Logger m_logger = LoggerFactory.getLogger(Actor.class);
	
	public Actor(IEntityTaskModelFactory taskModelFactory)
	{
		this(taskModelFactory, null);
	}
	
	public Actor(IEntityTaskModelFactory taskModelFactory, @Nullable String name)
	{
		super(taskModelFactory, name);
		m_bridge = new ActorBridge(new NullAudioClipFactory(), new NullFunctionFactory());
	}
	
	protected <Y extends Actor, T extends ActorBridge> Actor(IEntityTaskModelFactory taskModelFactory, IScriptFactory scriptFactory, ISceneModelFactory modelFactory, @Nullable String name, IImmutableVariable root, T entityContext) throws ValueSerializationException
	{
		super(taskModelFactory, scriptFactory, modelFactory, name, root, entityContext);
		m_bridge = entityContext;
	}

	public final DefaultEntity[] getVisibleEntities()
	{
		IEntity[] actorsInFov = getWorld().getEntities().search(new RadialSearchFilter<DefaultEntity>(getBody().getLocation().getXy(), getFieldOfView()));

		ArrayList<IEntity> visibleEntities = new ArrayList<>();

		float viewAngle = getBody().getDirection().getDirectionVector().getAngle();

		Vector2F hostLocation = getBody().getLocation().getXy();
		
		for (IEntity e : actorsInFov)
		{
			if (e == this)
				continue;
		
			Vector2F targetLocation = getBody().getLocation().getXy();
			
			float differenceAngle = Math.abs(viewAngle - targetLocation.difference(hostLocation).getAngle());
			
			float pathSight = TileEffects.merge(getWorld().getTileEffects(new TriangleSearchFilter<TileEffects>(hostLocation, targetLocation, targetLocation))).getSightEffect();

			if (getVisualAcuity() >= pathSight && differenceAngle <= getFieldOfView() / 2)
				visibleEntities.add(e);
		}

		return visibleEntities.toArray(new DefaultEntity[visibleEntities.size()]);
	}
	
	@Override
	@Nullable
	public String getDefaultCommand(DefaultEntity subject)
	{
		return m_script.getDefaultCommand(subject);
	}
	
	@Override
	public String[] getCommands(DefaultEntity subject)
	{
		return m_script.getCommands(subject);
	}

	@Override
	public void doCommand(DefaultEntity subject, String command)
	{
		m_script.doCommand(subject, command);
	}

	public float getVisibilityFactor() { return 0; }
	public float getViewDistance() { return 0; }
	public float getFieldOfView() { return 0; }
	public float getVisualAcuity() { return 0; }
	
	private class ActorBridgeNotifier
	{
		@Nullable
		public String getDefaultCommand(DefaultEntity subject)
		{
			try
			{
				return m_bridge.getDefaultCommand.hasHandler() ? m_bridge.getDefaultCommand.fire(subject.getBridge()) : null;
			} catch (ScriptExecuteException e)
			{
				m_logger.error("getDefaultCommand handler failed on entity " + getInstanceName(), e);
				return null;
			}
		}
		
		public String[] getCommands(DefaultEntity subject)
		{
			try
			{
				List<String> commands = m_bridge.getCommands.hasHandler() ? m_bridge.getCommands.fire(subject.getBridge()) : new ArrayList<String>();
				return commands.toArray(new String[commands.size()]);
			} catch (ScriptExecuteException e)
			{
				m_logger.error("getCommands handler failed on entity " + getInstanceName(), e);
				return new String[0];
			}
		}

		public void doCommand(IEntity subject, String command)
		{
			try
			{
				m_bridge.onCommand.fire(subject.getBridge(), command);
			} catch (ScriptExecuteException e)
			{
				m_logger.error("doCommand delegate failed on entity " + getInstanceName(), e);
			}
		}
	}
	
	public static class ActorBridge extends DefaultEntityBridge
	{
		private final Logger m_logger = LoggerFactory.getLogger(ActorBridge.class);
		
		private SearchForTask<?> m_searchTask;

		public final ScriptEvent onLookFound;
		public final ScriptEvent onCommand;
		public final ArrayScriptDelegate<String> getCommands;
		public final ScriptDelegate<String> getDefaultCommand;
		
		public ActorBridge(IAudioClipFactory audioClipFactory, IFunctionFactory functionFactory)
		{
			super(audioClipFactory, functionFactory);
		
			onLookFound = new ScriptEvent(functionFactory);
			onCommand = new ScriptEvent(functionFactory);
			getCommands = new ArrayScriptDelegate<>(functionFactory, String.class);
			getDefaultCommand = new ScriptDelegate<String>(functionFactory, String.class, true);
		}
		
		public void beginLook()
		{
			if(m_searchTask == null)
				m_searchTask = new ActorSearch((Actor)getEntity());
			
			IEntityTaskModel taskModel = getEntity().getTaskModel();
			
			if(!taskModel.isTaskActive(m_searchTask))
				taskModel.addTask(m_searchTask);
		}
		
		public void endLook()
		{
			IEntityTaskModel taskModel = getEntity().getTaskModel();
			
			if(m_searchTask != null && taskModel.isTaskActive(m_searchTask))
				taskModel.cancelTask(m_searchTask);
		}

		private class ActorSearch extends SearchForTask<Actor>
		{
			public ActorSearch(Actor me)
			{
				super(me, Actor.class);
			}
			
			@Override
			public void found(Actor actor)
			{
				try
				{
					ActorBridge.this.onLookFound.fire(actor.getBridge());
				}catch (ScriptExecuteException e)
				{
					m_logger.error("onLookFound delegate failed on entity " + ActorBridge.this.getEntity().getInstanceName(), e);
				}
			}

			@Override
			public void nothingFound() { }

			@Override
			public boolean continueSearch()
			{
				return true;
			}
		}
	}
}
