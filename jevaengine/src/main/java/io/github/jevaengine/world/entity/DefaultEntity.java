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
package io.github.jevaengine.world.entity;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.audio.NullAudioClipFactory;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.NullVariable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.script.IFunction;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.NullFunctionFactory;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.ScriptHiddenMember;
import io.github.jevaengine.script.ScriptableImmutableVariable;
import io.github.jevaengine.script.UnrecognizedFunctionException;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.WorldBridge;
import io.github.jevaengine.world.entity.tasks.IdleTask;
import io.github.jevaengine.world.entity.tasks.InvokeScriptFunctionTask;
import io.github.jevaengine.world.entity.tasks.InvokeScriptTimeoutFunctionTask;
import io.github.jevaengine.world.entity.tasks.PlayAudioTask;
import io.github.jevaengine.world.entity.tasks.SynchronousOneShotTask;
import io.github.jevaengine.world.physics.IImmutablePhysicsBody;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.IPhysicsBodyObserver;
import io.github.jevaengine.world.physics.NonparticipantPhysicsBody;
import io.github.jevaengine.world.physics.NullPhysicsBody;
import io.github.jevaengine.world.physics.PhysicsBodyDescription;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;
import io.github.jevaengine.world.scene.model.ISceneModel;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;
import io.github.jevaengine.world.scene.model.ISceneModelFactory.SceneModelConstructionException;
import io.github.jevaengine.world.scene.model.NullSceneModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEntity implements IEntity
{
	private static AtomicInteger m_unnamedCount = new AtomicInteger(0);

	private final Logger m_logger = LoggerFactory.getLogger(DefaultEntity.class);
	
	private final String m_name;

	private World m_world;
	
	private HashMap<String, Integer> m_flags = new HashMap<String, Integer>();
	private IImmutableVariable m_config = new NullVariable();
	private PhysicsBodyDescription m_physicsBodyDescription = null;
	
	private DefaultEntityBridge m_bridge;
	private final IEntityTaskModel m_taskModel;
	
	private ISceneModel m_model = new NullSceneModel();
	private IPhysicsBody m_body = new NullPhysicsBody();
	
	private final Observers m_observers = new Observers();
	
	private final EntityBridgeNotifier m_notifier = new EntityBridgeNotifier();
	
	public DefaultEntity(IEntityTaskModelFactory taskModelFactory)
	{
		this(taskModelFactory, null);
	}

	public DefaultEntity(IEntityTaskModelFactory taskModelFactory, @Nullable String name)
	{
		m_name = (name == null ? this.getClass().getName() + m_unnamedCount.getAndIncrement() : name);	
		m_bridge = new DefaultEntityBridge(new NullAudioClipFactory(), new NullFunctionFactory());

		m_bridge.setMe(this);
		
		m_taskModel = taskModelFactory.create(this);
	}
	
	public <T extends DefaultEntityBridge> DefaultEntity(IEntityTaskModelFactory taskModelFactory, IScriptFactory scriptFactory, ISceneModelFactory modelFactory, @Nullable String name, IImmutableVariable config, T entityContext) throws ValueSerializationException
	{
		m_name = (name == null ? this.getClass().getName() + m_unnamedCount.getAndIncrement() : name);	
		m_config = config;
		m_bridge = entityContext;
		m_bridge.setMe(this);
		
		DefaultEntityDeclaration entityConfig = new DefaultEntityDeclaration();
		entityConfig.deserialize(config);
		
		if (entityConfig.behavior != null && entityConfig.behavior.length() > 0)
		{
			try
			{
				scriptFactory.create(m_bridge, entityConfig.behavior);
				m_observers.add(m_notifier);
			}catch(AssetConstructionException e)
			{
				m_logger.error("Error instantiating behavior on entity " + m_name + ". Reverting to default behavior.", e);
			}
		}
		
		if(entityConfig.model != null)
		{
			try
			{
				m_model = modelFactory.create(entityConfig.model);
			} catch (SceneModelConstructionException e)
			{
				m_logger.error("Unable to instantiate model for entity " + m_name + ". Reverting to null model.", e);
			}
		}
		
		m_physicsBodyDescription = entityConfig.physicsBody;
		
		m_taskModel = taskModelFactory.create(this);
	}
	
	@Override
	public final void addObserver(IEntityObserver observer)
	{
		m_observers.add(observer);
	}

	@Override
	public final void removeObserver(IEntityObserver observer)
	{
		m_observers.remove(observer);
	}
	
	/*
	 * Physics body routines.
	 */
	private void constructPhysicsBody()
	{
		if(m_physicsBodyDescription == null)
			m_body = new NonparticipantPhysicsBody(this);
		else
			m_body = m_world.getPhysicsWorld().createBody(this, m_physicsBodyDescription);
		
		m_body.addObserver(m_notifier);
	}
	
	private void destroyPhysicsBody()
	{
		m_body.removeObserver(m_notifier);
		m_body.destory();
		m_body = new NullPhysicsBody();
	}
	
	@Override
	public final IPhysicsBody getBody()
	{
		return m_body;
	}
	
	/*
	 * Primitive entity property accessors.
	 */
	
	@Override
	public final String getInstanceName()
	{
		return m_name;
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	protected final IImmutableVariable getConfiguration()
	{
		return m_config;
	}
	
	/*
	 * Task model (adding/removing tasks) should be protected and accessed via the controller interface.
	 */
	protected final IEntityTaskModel getTaskModel()
	{
		return m_taskModel;
	}
	
	/*
	 * Flag operations.
	 */
	protected final void setFlag(String name, int value)
	{
		m_flags.put(name, value);
		m_observers.flagSet(name, value);
	}
		
	protected final void setFlag(String name)
	{
		setFlag(name, 0);
	}
		
	protected final void clearFlag(String name)
	{
		m_flags.remove(name);
		m_observers.flagCleared(name);
	}

	protected final void clearFlags()
	{
		m_flags.clear();
	}
	
	@Override
	public final Map<String, Integer> getFlags()
	{
		return Collections.unmodifiableMap(m_flags);
	}

	@Override
	public final int getFlag(String name)
	{
		Integer i = m_flags.get(name);
		
		if(i == null)
			throw new NoSuchElementException();
		
		return i;
	}

	@Override
	public final boolean testFlag(String name, int value)
	{
		return new Integer(value).equals(m_flags.get(name));
	}

	@Override
	public final boolean isFlagSet(String name)
	{
		return m_flags.containsKey(name);
	}
	
	/*
	 * World association routines.
	 */
	@Override
	public final World getWorld()
	{
		return m_world;
	}

	@Override
	public final void associate(World world)
	{
		if (m_world != null)
			throw new WorldAssociationException("Already associated with world");

		m_world = world;

		m_observers.enterWorld();
		
		constructPhysicsBody();
	}

	@Override
	public final void disassociate()
	{
		if (m_world == null)
			throw new WorldAssociationException("Not associated with world");

		destroyPhysicsBody();
		
		m_taskModel.cancelTasks();

		m_observers.leaveWorld();

		m_world = null;
	}
	
	/*
	 * Scene model methods...
	 */
	@Override
	public final IImmutableSceneModel getModel()
	{
		return m_model;
	}
	
	protected final ISceneModel getMutableModel()
	{
		return m_model;
	}
	
	@Override
	public final IEntityBridge getBridge()
	{
		return m_bridge;
	}

	@Override
	public final void update(int deltaTime)
	{
		if (m_world == null)
			throw new WorldAssociationException("Entity is unassociated with a world and thus cannot process logic.");

		m_taskModel.update(deltaTime);
		m_model.update(deltaTime);
		doLogic(deltaTime);
	}
	
	protected void doLogic(int deltaTime) { }
	
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
	
		public void flagSet(String name, int value)
		{
			for (IEntityObserver observer : this)
				observer.flagSet(name, value);
		}
		
		public void flagCleared(String name)
		{
			for (IEntityObserver observer : this)
				observer.flagCleared(name);
		}
	}

	private class EntityBridgeNotifier implements IEntityObserver, IPhysicsBodyObserver
	{
		@Override
		public void enterWorld()
		{
			try
			{
				m_bridge.onEnter.fire();
			} catch (ScriptExecuteException e)
			{
				m_logger.error("onEnter delegate failed on entity " + getInstanceName(), e);
			}
		}
		
		@Override
		public void leaveWorld()
		{
			try
			{
				m_bridge.onLeave.fire();
			} catch (ScriptExecuteException e)
			{
				m_logger.error("onLeave delegate failed on entity " + getInstanceName(), e);
			}
		}

		@Override
		public void flagSet(String name, int value)
		{
			try
			{
				m_bridge.onFlagSet.fire(name, value);
			} catch (ScriptExecuteException e)
			{
				m_logger.error("onFlagSet delegate failed on entity " + getInstanceName(), e);
			}
		}

		@Override
		public void flagCleared(String name)
		{
			try
			{
				m_bridge.onFlagCleared.fire(name);
			} catch (ScriptExecuteException e)
			{
				m_logger.error("onFlagCleared delegate failed on entity " + getInstanceName(), e);
			}
		}
		
		@Override
		public void locationSet()
		{
			try
			{
				m_bridge.onLocationSet.fire();;
			} catch (ScriptExecuteException e)
			{
				m_logger.error("onLocationSet delegate failed on entity " + getInstanceName(), e);
			}
		}

		@Override
		public void directionSet() { }

		@Override
		public void onBeginContact(IImmutablePhysicsBody other) { }

		@Override
		public void onEndContact(IImmutablePhysicsBody other) { }
	}
	
	public static class DefaultEntityBridge implements IEntityBridge
	{
		private Logger m_logger = LoggerFactory.getLogger(DefaultEntityBridge.class);
		
		private final IAudioClipFactory m_audioClipFactory;
		private final IFunctionFactory m_functionFactory;
		
		public final ScriptEvent onEnter;
		public final ScriptEvent onLeave;
		public final ScriptEvent onFlagSet;
		public final ScriptEvent onFlagCleared;
		public final ScriptEvent onLocationSet;

		private final HashMap<String, IFunction> m_interfaceMapping = new HashMap<>();
		
		private DefaultEntity m_me;
		
		public DefaultEntityBridge(IAudioClipFactory audioClipFactory, IFunctionFactory functionFactory)
		{
			m_audioClipFactory = audioClipFactory;
			m_functionFactory = functionFactory;
			
			onEnter = new ScriptEvent(functionFactory);
			onLeave = new ScriptEvent(functionFactory);
			onFlagSet = new ScriptEvent(functionFactory);
			onFlagCleared = new ScriptEvent(functionFactory);
			onLocationSet = new ScriptEvent(functionFactory);
		}
		
		protected final void setMe(DefaultEntity me)
		{
			m_me = me;
		}
		
		@ScriptHiddenMember
		public final DefaultEntity getEntity()
		{
			return m_me;
		}
		
		public final String getName()
		{
			return getEntity().getInstanceName();
		}
		
		@Nullable
		public final WorldBridge getWorld()
		{
			World world = getEntity().getWorld();
			
			return world == null ? null : world.getScriptBridge();
		}
		
		public final void log(String message)
		{
			m_logger.info("Log from " + m_me.getInstanceName() + ", " + message);
		}
		
		public final void mapInterface(String interfaceName, Object rawFunction)
		{
			try
			{
				m_interfaceMapping.put(interfaceName, m_functionFactory.wrap(rawFunction));
				
			} catch (UnrecognizedFunctionException e)
			{
				m_logger.error("Unable to map interface " + interfaceName + " on entity" + m_me.getInstanceName(), e);
			}
		}
		
		public final Object invokeInterface(String interfaceName, Object ... arguments) throws NoSuchInterfaceException, InterfaceExecuteException
		{
			IFunction function = m_interfaceMapping.get(interfaceName);
			
			if(function == null)
				throw new NoSuchInterfaceException(interfaceName);
			
			try
			{
				return function.call(arguments);
			} catch (ScriptExecuteException e) {
				throw new InterfaceExecuteException(interfaceName, e);
			}
		}

		public final Vector3F getLocation()
		{
			return getEntity().getBody().getLocation();
		}

		public final void setLocation(Vector3F location)
		{
			getEntity().getBody().setLocation(location);
		}

		public final void setLocation(float x, float y, float z)
		{
			getEntity().getBody().setLocation(new Vector3F(x, y, z));
		}

		public final void cancelTasks()
		{
			getEntity().getTaskModel().cancelTasks();
		}

		public final void playAudio(String audioName)
		{
			try
			{
				getEntity().getTaskModel().addTask(new PlayAudioTask(m_audioClipFactory.create(audioName)));
			} catch (AssetConstructionException e)
			{
				m_logger.error("Error playing audio on entity " + getEntity().getInstanceName(), e);
			}
		}
		
		public final void invoke(Object target, Object ... parameters)
		{
			try
			{
				getEntity().getTaskModel().addTask(new InvokeScriptFunctionTask(m_functionFactory.wrap(target), parameters));
			}catch(UnrecognizedFunctionException e)
			{
				m_logger.error("Error wrapping invoke target on entity " + getEntity().getInstanceName(), e);
			}
		}
		
		public final void invokeTimeout(int timeout, Object target, Object ... parameters)
		{
			try
			{
				getEntity().getTaskModel().addTask(new InvokeScriptTimeoutFunctionTask(timeout, m_functionFactory.wrap(target), parameters));
			} catch(UnrecognizedFunctionException e)
			{
				m_logger.error("Error wrapping invoke target on entity " + getEntity().getInstanceName(), e);
			}
		}

		public final void idle(int length)
		{
			getEntity().getTaskModel().addTask(new IdleTask(length));
		}

		public final void leave()
		{
			getEntity().getTaskModel().addTask(new SynchronousOneShotTask()
			{
				@Override
				public void run(IEntity entity)
				{
					getEntity().getWorld().removeEntity(getEntity());
				}
			});
		}

		public final boolean testFlag(String name, int value)
		{
			return getEntity().testFlag(name, value);
		}
		
		public final void setFlag(String name, int value)
		{
			getEntity().setFlag(name, value);
		}
		
		public final int getFlag(String name)
		{
			return getEntity().getFlag(name);
		}
		
		public final void clearFlag(String name)
		{
			getEntity().clearFlag(name);
		}
		
		public final void clearFlags()
		{
			getEntity().clearFlags();
		}
		
		public final boolean isFlagSet(String name)
		{
			return getEntity().isFlagSet(name);
		}
		
		public final ScriptableImmutableVariable getConfiguration()
		{
			return new ScriptableImmutableVariable(m_me.m_config);
		}
	}
	
	public static class DefaultEntityDeclaration implements ISerializable
	{
		public String behavior;
		public String model;
		public PhysicsBodyDescription physicsBody;
		
		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			if(behavior != null && behavior.length() > 0)
				target.addChild("behavior").setValue(behavior);
		
			if(model != null && model.length() > 0)
				target.addChild("model").setValue(model);
			
			if(physicsBody != null)
				target.addChild("physicsBody").setValue(physicsBody);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				if(source.childExists("behavior"))
					behavior = source.getChild("behavior").getValue(String.class);
			
				if(source.childExists("model"))
					model = source.getChild("model").getValue(String.class);
			
				if(source.childExists("physicsBody"))
					physicsBody = source.getChild("physicsBody").getValue(PhysicsBodyDescription.class);
				
			} catch(NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}
	}
}
