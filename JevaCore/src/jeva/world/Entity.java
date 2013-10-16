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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.ScriptException;

import com.sun.istack.internal.Nullable;

import proguard.annotation.KeepClassMemberNames;
import sun.org.mozilla.javascript.internal.Scriptable;
import jeva.Core;
import jeva.CoreScriptException;
import jeva.IResourceLibrary;
import jeva.Script;
import jeva.StatelessEnvironmentException;
import jeva.UnresolvedResourcePathException;
import jeva.audio.Audio;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.util.StaticSet;


public abstract class Entity extends Variable implements IWorldAssociation
{

	/** The m_unnamed count. */
	private static AtomicInteger m_unnamedCount = new AtomicInteger(0);

	/** The m_pending tasks. */
	private ArrayList<ITask> m_pendingTasks = new ArrayList<ITask>();

	/** The m_running tasks. */
	private ArrayList<ITask> m_runningTasks = new ArrayList<ITask>();

	/** The m_entity script. */
	private EntityScript m_entityScript = new EntityScript();

	/** The m_is paused. */
	private boolean m_isPaused = false;

	/** The m_bridge. */
	private EntityBridge<?> m_bridge;

	/** The m_script variables. */
	private Variable m_scriptVariables;

	/** The m_parent world. */
	private World m_parentWorld;

	/** The m_observers. */
	private Observers m_observers = new Observers();

	public Entity()
	{
		super("__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement());
	}

	
	public Entity(@Nullable String name)
	{
		super(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);
	}

	
	protected <T extends EntityBridge<?>> Entity(@Nullable String name, Variable root, T entityContext)
	{
		super(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);

		init(root, entityContext);
	}

	
	protected <T extends EntityBridge<?>> Entity(@Nullable String name, Variable root, T entityContext, WorldDirection direction)
	{
		super(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);

		init(root, entityContext);
	}

	
	private <T extends EntityBridge<?>> void init(Variable root, T entityContext)
	{
		m_scriptVariables = root;

		entityContext.setMe(this);

		m_bridge = entityContext;

		if (root.variableExists("script"))
		{
			String script = Core.getService(IResourceLibrary.class).openResourceContents(root.getVariable("script").getValue().getString());

			m_entityScript = new EntityScript(script, entityContext);

			m_observers.add(m_entityScript);

		} else
			m_entityScript = new EntityScript();
	}

	
	public void addObserver(IEntityObserver observer)
	{
		m_observers.add(observer);
	}

	
	public void removeObserver(IEntityObserver observer)
	{
		m_observers.remove(observer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.IWorldAssociation#isAssociated()
	 */
	public final boolean isAssociated()
	{
		return m_parentWorld != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.IWorldAssociation#associate(jeva.world.World)
	 */
	public final void associate(World world)
	{
		if (m_parentWorld != null)
			throw new WorldAssociationException("Already associated with world");

		m_parentWorld = world;

		m_observers.enterWorld();
		m_observers.taskBusyState(isIdle());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.IWorldAssociation#disassociate()
	 */
	public final void disassociate()
	{
		if (m_parentWorld == null)
			throw new WorldAssociationException("Not associated with world");

		this.cancelTasks();

		m_observers.leaveWorld();

		m_parentWorld = null;
	}

	
	protected final Variable getEntityVariables()
	{
		return m_scriptVariables;
	}

	
	public final EntityBridge<?> getScriptBridge()
	{
		return m_bridge;
	}

	
	public final void pause()
	{
		m_isPaused = true;
	}

	
	public final void resume()
	{
		m_isPaused = false;
	}

	
	public final boolean isPaused()
	{
		return m_isPaused;
	}

	
	protected final void addTask(ITask task)
	{
		m_pendingTasks.add(task);
	}

	
	protected final void cancelTasks()
	{
		for (ITask task : m_runningTasks)
			task.cancel();

		m_pendingTasks.clear();
	}

	
	public void cancelTask(ITask task)
	{
		if (!m_runningTasks.contains(task))
			throw new NoSuchElementException();

		task.cancel();
	}

	
	protected final boolean isTaskActive(ITask task)
	{
		return m_runningTasks.contains(task);
	}

	
	private boolean isTaskBlocking()
	{
		for (ITask task : m_runningTasks)
		{
			if (!task.isParallel())
				return true;
		}

		return false;
	}

	
	public final boolean isIdle()
	{
		return m_runningTasks.isEmpty() && m_pendingTasks.isEmpty();
	}

	
	public final World getWorld()
	{
		return m_parentWorld;
	}

	
	public Script getScript()
	{
		return m_entityScript;
	}

	
	public final void update(int deltaTime)
	{
		boolean oldIsIdle = isIdle();

		if (!this.isAssociated())
			throw new WorldAssociationException("Entity is unassociated with a world and thus cannot process logic.");

		if (!isPaused())
			doLogic(deltaTime);

		ArrayList<ITask> garbageTasks = new ArrayList<ITask>();

		LinkedList<ITask> reassignList = new LinkedList<ITask>();

		for (ITask task : m_pendingTasks)
		{
			if ((task.ignoresPause() && isPaused()) || !isPaused() && ((task.isParallel() && isTaskBlocking()) || !isTaskBlocking()))
			{
				if (reassignList.peekLast() == null || reassignList.peekLast().isParallel())
				{
					task.begin(this);
					reassignList.add(task);
				}
			}
		}

		for (ITask task : reassignList)
		{
			m_runningTasks.add(task);
			m_pendingTasks.remove(task);
		}

		if (!m_runningTasks.isEmpty())
		{
			for (ITask task : m_runningTasks)
			{
				if (!isPaused() || (isPaused() && task.ignoresPause()))
				{
					if (task.doCycle(deltaTime))
						garbageTasks.add(task);

					if (!task.isParallel())
						break;
				}
			}
		}

		for (ITask task : garbageTasks)
		{
			task.end();
			m_runningTasks.remove(task);
		}

		if (isIdle() != oldIsIdle && !this.isPaused())
		{
			m_observers.taskBusyState(isIdle());
		}
	}

	
	public abstract void blendEffectMap(EffectMap globalEffectMap);

	
	public abstract void doLogic(int deltaTime);

	
	public interface IEntityObserver
	{

		
		void enterWorld();

		
		void leaveWorld();

		
		void taskBusyState(boolean isBusy);
	}

	
	private class Observers extends StaticSet<IEntityObserver>
	{

		
		public void enterWorld()
		{
			for (IEntityObserver observer : this)
				observer.enterWorld();
		}

		
		public void leaveWorld()
		{
			for (IEntityObserver observer : this)
				observer.leaveWorld();
		}

		
		public void taskBusyState(boolean isBusy)
		{
			for (IEntityObserver observer : this)
				observer.taskBusyState(isBusy);
		}
	}

	
	private class EntityScript extends Script implements IEntityObserver
	{

		
		public EntityScript()
		{
		}

		
		public EntityScript(String script, Object context)
		{
			setScript(script, context);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Entity.IEntityObserver#enterWorld()
		 */
		@Override
		public void enterWorld()
		{
			if (isReady())
			{
				try
				{
					invokeScriptFunction("onEnter");
				} catch (NoSuchMethodException e)
				{
					// Startup is an optional implementation for entities.
				} catch (ScriptException e)
				{
					throw new CoreScriptException("Error executing NPC Script onEnter routine for " + Entity.this.getFullName() + ", " + e.getMessage());
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Entity.IEntityObserver#leaveWorld()
		 */
		@Override
		public void leaveWorld()
		{
			if (isReady())
			{
				try
				{
					invokeScriptFunction("onLeave");
				} catch (NoSuchMethodException e)
				{
					// Startup is an optional implementation for entities.
				} catch (ScriptException e)
				{
					throw new CoreScriptException("Error executing entity Script onLeave routine of " + Entity.this.getName() + " " + e.getMessage());
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.Entity.IEntityObserver#taskBusyState(boolean)
		 */
		@Override
		public void taskBusyState(boolean isBusy)
		{
			if (isReady())
			{
				try
				{
					invokeScriptFunction("taskBusyState", isBusy);
				} catch (NoSuchMethodException e)
				{

				} catch (ScriptException e)
				{
					throw new CoreScriptException("Error occured while invoking onIdle: " + e.getMessage());
				}
			}
		}
	}

	
	@KeepClassMemberNames
	public static class EntityBridge<T extends Entity>
	{

		/** The m_me. */
		private Entity m_me;
		
		protected final void setMe(Entity me)
		{
			m_me = me;
		}

		
		@SuppressWarnings("unchecked")
		protected T getMe()
		{
			return (T) m_me;
		}

		
		public String getName()
		{
			return getMe().getName();
		}

		
		public void cancelTasks()
		{
			getMe().cancelTasks();
		}

		
		public void pause()
		{
			getMe().pause();
		}

		
		public void resume()
		{
			getMe().resume();
		}

		
		public void playAudio(String audioName)
		{
			getMe().addTask(new PlayAudioTask(new Audio(audioName), false));
		}

		
		public boolean isIdle()
		{
			return getMe().isIdle();
		}

		
		public void idle(int length)
		{
			getMe().addTask(new IdleTask(length));
		}

		
		public void leave()
		{
			getMe().addTask(new SynchronousOneShotTask()
			{
				@Override
				public void run(Entity entity)
				{
					getMe().getWorld().removeEntity(getMe());
				}
			});
		}

		
		private static void loadState(Entity entity, Variable srcProperty, String parentName)
		{
			if (!srcProperty.getValue().getString().isEmpty())
				entity.setVariable(parentName, srcProperty.getValue());

			for (Variable var : srcProperty)
				loadState(entity, var, (parentName.length() > 0 ? parentName + Variable.NAME_SPLIT : "") + var.getName());
		}

		
		public boolean loadState(String state)
		{
			try
			{
				VariableStore varStore = VariableStore.create(Core.getService(IResourceLibrary.class).openState(state));

				for (Variable v : varStore)
				{
					loadState((T) getMe(), v, v.getName());
				}
				return true;

			} catch (UnresolvedResourcePathException | StatelessEnvironmentException e)
			{
				return false;
			}
		}

		
		public boolean storeState(String state)
		{
			try
			{
				getMe().serialize(Core.getService(IResourceLibrary.class).createState(state));
			} catch (StatelessEnvironmentException e)
			{
				return false;
			}

			return true;
		}
		
		public Scriptable getScript()
		{
			if(!getMe().getScript().isReady())
				return null;
			
			return getMe().getScript().getScriptedInterface();
		}
	}
}
