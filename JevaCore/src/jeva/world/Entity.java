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

/**
 * The Class Entity.
 * 
 * @author Jeremy. A. W
 */
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

	/*
	 * All subclass entities to be constructed via map loading must include the
	 * constructors: Entity(String name, List<VariableValue> arguments)
	 * 
	 * If they represent a tile, they can omit name:
	 * 
	 * Entity(List<VariableValue> initArgs);
	 */

	/**
	 * Instantiates a new entity.
	 */
	public Entity()
	{
		super("__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement());
	}

	/**
	 * Instantiates a new entity.
	 * 
	 * @param name
	 *            the name
	 */
	public Entity(@Nullable String name)
	{
		super(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);
	}

	/**
	 * Instantiates a new entity.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param name
	 *            the name
	 * @param root
	 *            the root
	 * @param entityContext
	 *            the entity context
	 */
	protected <T extends EntityBridge<?>> Entity(@Nullable String name, Variable root, T entityContext)
	{
		super(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);

		init(root, entityContext);
	}

	/**
	 * Instantiates a new entity.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param name
	 *            the name
	 * @param root
	 *            the root
	 * @param entityContext
	 *            the entity context
	 * @param direction
	 *            the direction
	 */
	protected <T extends EntityBridge<?>> Entity(@Nullable String name, Variable root, T entityContext, WorldDirection direction)
	{
		super(name == null ? "__UNNAMED_ENTITY" + m_unnamedCount.getAndIncrement() : name);

		init(root, entityContext);
	}

	/**
	 * Inits the.
	 * 
	 * @param <T>
	 *            the generic type
	 * @param root
	 *            the root
	 * @param entityContext
	 *            the entity context
	 */
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

	/**
	 * Adds the observer.
	 * 
	 * @param observer
	 *            the observer
	 */
	public void addObserver(IEntityObserver observer)
	{
		m_observers.add(observer);
	}

	/**
	 * Removes the observer.
	 * 
	 * @param observer
	 *            the observer
	 */
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

	/**
	 * Gets the entity variables.
	 * 
	 * @return the entity variables
	 */
	protected final Variable getEntityVariables()
	{
		return m_scriptVariables;
	}

	/**
	 * Gets the script bridge.
	 * 
	 * @return the script bridge
	 */
	public final EntityBridge<?> getScriptBridge()
	{
		return m_bridge;
	}

	/**
	 * Pause.
	 */
	public final void pause()
	{
		m_isPaused = true;
	}

	/**
	 * Resume.
	 */
	public final void resume()
	{
		m_isPaused = false;
	}

	/**
	 * Checks if is paused.
	 * 
	 * @return true, if is paused
	 */
	public final boolean isPaused()
	{
		return m_isPaused;
	}

	/**
	 * Adds the task.
	 * 
	 * @param task
	 *            the task
	 */
	protected final void addTask(ITask task)
	{
		m_pendingTasks.add(task);
	}

	/**
	 * Cancel tasks.
	 */
	protected final void cancelTasks()
	{
		for (ITask task : m_runningTasks)
			task.cancel();

		m_pendingTasks.clear();
	}

	/**
	 * Cancel task.
	 * 
	 * @param task
	 *            the task
	 */
	public void cancelTask(ITask task)
	{
		if (!m_runningTasks.contains(task))
			throw new NoSuchElementException();

		task.cancel();
	}

	/**
	 * Checks if is task active.
	 * 
	 * @param task
	 *            the task
	 * @return true, if is task active
	 */
	protected final boolean isTaskActive(ITask task)
	{
		return m_runningTasks.contains(task);
	}

	/**
	 * Checks if is task blocking.
	 * 
	 * @return true, if is task blocking
	 */
	private boolean isTaskBlocking()
	{
		for (ITask task : m_runningTasks)
		{
			if (!task.isParallel())
				return true;
		}

		return false;
	}

	/**
	 * Checks if is idle.
	 * 
	 * @return true, if is idle
	 */
	public final boolean isIdle()
	{
		return m_runningTasks.isEmpty() && m_pendingTasks.isEmpty();
	}

	/**
	 * Gets the world.
	 * 
	 * @return the world
	 */
	public final World getWorld()
	{
		return m_parentWorld;
	}

	/**
	 * Gets the script.
	 * 
	 * @return the script
	 */
	public Script getScript()
	{
		return m_entityScript;
	}

	/**
	 * Update.
	 * 
	 * @param deltaTime
	 *            the delta time
	 */
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

	/**
	 * Blend effect map.
	 * 
	 * @param globalEffectMap
	 *            the global effect map
	 */
	public abstract void blendEffectMap(EffectMap globalEffectMap);

	/**
	 * Do logic.
	 * 
	 * @param deltaTime
	 *            the delta time
	 */
	public abstract void doLogic(int deltaTime);

	/**
	 * An asynchronous update interface for receiving notifications about
	 * IEntity information as the IEntity is constructed.
	 */
	public interface IEntityObserver
	{

		/**
		 * This method is called when information about an IEntity which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 */
		void enterWorld();

		/**
		 * This method is called when information about an IEntity which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 */
		void leaveWorld();

		/**
		 * This method is called when information about an IEntity which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 * 
		 * @param isBusy
		 *            the is busy
		 */
		void taskBusyState(boolean isBusy);
	}

	/**
	 * The Class Observers.
	 */
	private class Observers extends StaticSet<IEntityObserver>
	{

		/**
		 * Enter world.
		 */
		public void enterWorld()
		{
			for (IEntityObserver observer : this)
				observer.enterWorld();
		}

		/**
		 * Leave world.
		 */
		public void leaveWorld()
		{
			for (IEntityObserver observer : this)
				observer.leaveWorld();
		}

		/**
		 * Task busy state.
		 * 
		 * @param isBusy
		 *            the is busy
		 */
		public void taskBusyState(boolean isBusy)
		{
			for (IEntityObserver observer : this)
				observer.taskBusyState(isBusy);
		}
	}

	/**
	 * The Class EntityScript.
	 */
	private class EntityScript extends Script implements IEntityObserver
	{

		/**
		 * Instantiates a new entity script.
		 */
		public EntityScript()
		{
		}

		/**
		 * Instantiates a new entity script.
		 * 
		 * @param script
		 *            the script
		 * @param context
		 *            the context
		 */
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
			if (isScriptReady())
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
			if (isScriptReady())
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
			if (isScriptReady())
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

	/**
	 * The Class EntityBridge.
	 * 
	 * @param <T>
	 *            the generic type
	 */
	@KeepClassMemberNames
	public static class EntityBridge<T extends Entity>
	{

		/** The m_me. */
		private Entity m_me;

		/**
		 * Sets the me.
		 * 
		 * @param me
		 *            the new me
		 */
		protected void setMe(Entity me)
		{
			m_me = me;
		}

		/**
		 * Gets the me.
		 * 
		 * @return the me
		 */
		@SuppressWarnings("unchecked")
		protected T getMe()
		{
			return (T) m_me;
		}

		/**
		 * Gets the name.
		 * 
		 * @return the name
		 */
		public String getName()
		{
			return getMe().getName();
		}

		/**
		 * Cancel tasks.
		 */
		public void cancelTasks()
		{
			getMe().cancelTasks();
		}

		/**
		 * Pause.
		 */
		public void pause()
		{
			getMe().pause();
		}

		/**
		 * Resume.
		 */
		public void resume()
		{
			getMe().resume();
		}

		/**
		 * Play audio.
		 * 
		 * @param audioName
		 *            the audio name
		 */
		public void playAudio(String audioName)
		{
			getMe().addTask(new PlayAudioTask(new Audio(audioName), false));
		}

		/**
		 * Checks if is idle.
		 * 
		 * @return true, if is idle
		 */
		public boolean isIdle()
		{
			return getMe().isIdle();
		}

		/**
		 * Idle.
		 * 
		 * @param length
		 *            the length
		 */
		public void idle(int length)
		{
			getMe().addTask(new IdleTask(length));
		}

		/**
		 * Leave.
		 */
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

		/**
		 * Load state.
		 * 
		 * @param entity
		 *            the entity
		 * @param srcProperty
		 *            the src property
		 * @param parentName
		 *            the parent name
		 */
		private static void loadState(Entity entity, Variable srcProperty, String parentName)
		{
			if (!srcProperty.getValue().getString().isEmpty())
				entity.setVariable(parentName, srcProperty.getValue());

			for (Variable var : srcProperty)
				loadState(entity, var, (parentName.length() > 0 ? parentName + Variable.NAME_SPLIT : "") + var.getName());
		}

		/**
		 * Load state.
		 * 
		 * @param state
		 *            the state
		 * @return true, if successful
		 */
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

		/**
		 * Store state.
		 * 
		 * @param state
		 *            the state
		 * @return true, if successful
		 */
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
	}
}
