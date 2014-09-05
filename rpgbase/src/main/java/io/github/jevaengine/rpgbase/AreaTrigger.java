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
package io.github.jevaengine.rpgbase;

import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.ISerializable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.NullVariable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.rpgbase.character.RpgCharacter;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.IScriptFactory.ScriptConstructionException;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.script.ScriptableImmutableVariable;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.entity.DefaultEntity;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.WorldAssociationException;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.NonparticipantPhysicsBody;
import io.github.jevaengine.world.physics.NullPhysicsBody;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;
import io.github.jevaengine.world.scene.model.NullSceneModel;
import io.github.jevaengine.world.search.RectangleSearchFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AreaTrigger implements IEntity
{
	private static final int SCAN_INTERVAL = 400;
	
	private static final AtomicInteger m_unnamedCount = new AtomicInteger();
	
	private final Logger m_logger = LoggerFactory.getLogger(AreaTrigger.class);
	private final Observers m_observers = new Observers();

	private World m_world;
	
	private final String m_name;
	
	private int m_lastScan;	
	private ArrayList<RpgCharacter> m_includedEntities = new ArrayList<RpgCharacter>();

	private float m_width;
	private float m_height;
	
	private AreaTriggerBridge m_bridge;

	private IPhysicsBody m_body = new NullPhysicsBody();
	
	private final IImmutableVariable m_arguments;
	
	public AreaTrigger(IScriptFactory scriptFactory, @Nullable String name, IImmutableVariable config) throws ValueSerializationException
	{
		m_name = name == null ? this.getClass().getName() + m_unnamedCount.getAndIncrement() : name;
		
		AreaTriggerDeclaration decl = config.getValue(AreaTriggerDeclaration.class);
		m_arguments = decl.arguments;
		m_width = decl.width;
		m_height = decl.height;	

		m_bridge = new AreaTriggerBridge(this, scriptFactory.getFunctionFactory());
		
		if(decl.behavior != null)
		{
			try {
				scriptFactory.create(m_bridge, decl.behavior);
			} catch (ScriptConstructionException e) {
				m_logger.error("Unable to instantiate behavior for entity " + name + " defaulting to null behavior.", e);
			}
		}

		m_observers.add(new BridgeNotifier());
	}

	private Rect2F getContainingBounds()
	{
		Vector2F location = getBody().getLocation().getXy();
		
		//The bounds for an area trigger are meant to be interpreted as containing
		//whole tiles. I.e, a width of 1, and height of 1, located at 0,0 should contain the tile 0,0.
		//The way the engine interprets a rect located at 0,0 with a width 1 and height 1 is to have it start
		//from the origin of 0,0 (the center of the tile at 0,0). In this case, we want it to start
		//from the corner, thus containing the entire tile...

		return new Rect2F(location.x - 0.5F, location.y - 0.5F, m_width, m_height);
	}
	
	@Override
	public String getInstanceName()
	{
		return m_name;
	}
	
	@Override
	public World getWorld()
	{
		return m_world;
	}

	@Override
	public void associate(World world)
	{
		if (m_world != null)
			throw new WorldAssociationException("Already associated with world");

		m_world = world;

		constructPhysicsBody();
		m_observers.enterWorld();
	}

	@Override
	public void disassociate()
	{
		if (m_world == null)
			throw new WorldAssociationException("Not associated with world");

		m_observers.leaveWorld();
		destroyPhysicsBody();

		m_world = null;	
	}

	private void constructPhysicsBody()
	{
		m_body = new NonparticipantPhysicsBody(this);
	}
	
	private void destroyPhysicsBody()
	{
		m_body.destory();
		m_body = new NullPhysicsBody();
	}
	
	@Override
	public boolean isStatic()
	{
		return false;
	}
	
	@Override
	public Map<String, Integer> getFlags()
	{
		return new HashMap<>();
	}

	@Override
	public int getFlag(String name)
	{
		return 0;
	}

	@Override
	public boolean testFlag(String name, int value)
	{
		return false;
	}

	@Override
	public boolean isFlagSet(String name)
	{
		return false;
	}

	@Override
	public IImmutableSceneModel getModel()
	{
		return new NullSceneModel();
	}

	@Override
	public IPhysicsBody getBody()
	{
		return m_body;
	}

	@Override
	public void addObserver(IEntityObserver o)
	{
		m_observers.add(o);
	}

	@Override
	public void removeObserver(IEntityObserver o)
	{
		m_observers.remove(o);
	}
	
	public void addAreaTriggerObserver(IAreaTriggerObserver o)
	{
		m_observers.add(o);
	}
	
	public void removeAreaTriggerObserver(IAreaTriggerObserver o)
	{
		m_observers.remove(o);
	}
	
	@Override
	public void update(int deltaTime)
	{
		m_lastScan -= deltaTime;

		if (m_lastScan <= 0)
		{
			m_lastScan = SCAN_INTERVAL;

			IEntity[] entities = getWorld().getEntities().search(new RectangleSearchFilter<DefaultEntity>(getContainingBounds()));

			ArrayList<RpgCharacter> unfoundCharacters = new ArrayList<RpgCharacter>(m_includedEntities);

			for (IEntity entity : entities)
			{
				if (!(entity instanceof RpgCharacter))
					continue;

				RpgCharacter character = (RpgCharacter)entity;

				if (!unfoundCharacters.contains(character))
				{
					m_includedEntities.add(character);
					m_observers.enter(character);
					character.addObserver(new TriggerCharacterObserver(character));
				} else
				{
					unfoundCharacters.remove(character);
				}
			}

			for (RpgCharacter character : unfoundCharacters)
			{
				m_includedEntities.remove(character);
				m_observers.leave(character);
			}
		}
	}

	@Override
	public IEntityBridge getBridge()
	{
		return m_bridge;
	}
	
	private final class BridgeNotifier implements IAreaTriggerObserver
	{

		@Override
		public void enter(RpgCharacter character)
		{
			try
			{
				m_bridge.onAreaEnter.fire(character.getBridge());
			}catch(ScriptExecuteException e)
			{
				m_logger.error("onAreaEnter delegate failed on entity " + getInstanceName(), e);
			}
		}

		@Override
		public void leave(RpgCharacter character)
		{
			try
			{
				m_bridge.onAreaLeave.fire(character.getBridge());
			}catch(ScriptExecuteException e)
			{
				m_logger.error("onAreaLeave delegate failed on entity " + getInstanceName(), e);
			}	
		}
		
	}
	
	public interface IAreaTriggerObserver
	{
		void enter(RpgCharacter character);
		void leave(RpgCharacter character);
	}

	private static class Observers
	{
		private final StaticSet<IAreaTriggerObserver> m_areaTriggerObservers = new StaticSet<>();
		private final StaticSet<IEntityObserver> m_entityObservers = new StaticSet<>();
		
		public void add(IAreaTriggerObserver o)
		{
			m_areaTriggerObservers.add(o);
		}
		
		public void add(IEntityObserver o)
		{
			m_entityObservers.add(o);
		}

		public void remove(IAreaTriggerObserver o)
		{
			m_areaTriggerObservers.remove(o);
		}
		
		public void remove(IEntityObserver o)
		{
			m_entityObservers.remove(o);
		}
		
		void enter(RpgCharacter character)
		{
			for(IAreaTriggerObserver o : m_areaTriggerObservers)
				o.enter(character);
		}
		
		void leave(RpgCharacter character)
		{
			for(IAreaTriggerObserver o : m_areaTriggerObservers)
				o.leave(character);
		}

		public void enterWorld()
		{
			for(IEntityObserver o : m_entityObservers)
				o.enterWorld();
		}

		public void leaveWorld()
		{
			for(IEntityObserver o : m_entityObservers)
				o.leaveWorld();
		}
	}
	
	public static class AreaTriggerBridge implements IEntityBridge
	{
		private final AreaTrigger m_host;
		
		public final ScriptEvent onAreaEnter;
		public final ScriptEvent onAreaLeave;
		
		public AreaTriggerBridge(AreaTrigger host, IFunctionFactory functionFactory)
		{
			m_host = host;
			onAreaEnter = new ScriptEvent(functionFactory);
			onAreaLeave = new ScriptEvent(functionFactory);
		}

		@Override
		public IEntity getEntity()
		{
			return m_host;
		}

		@Override
		public String getName()
		{
			return m_host.getInstanceName();
		}

		@Override
		public Vector3F getLocation()
		{
			return m_host.getBody().getLocation();
		}

		@Override
		public boolean isFlagSet(String name)
		{
			return false;
		}

		@Override
		public int getFlag(String name)
		{
			return 0;
		}

		@Override
		public Object invokeInterface(String interfaceName, Object... arguments) throws NoSuchInterfaceException, InterfaceExecuteException
		{
			throw new NoSuchInterfaceException(interfaceName);
		}
		
		public ScriptableImmutableVariable getArguments()
		{
			return new ScriptableImmutableVariable(m_host.m_arguments);
		}
	}

	private class TriggerCharacterObserver implements IEntityObserver
	{
		private RpgCharacter m_observee;

		public TriggerCharacterObserver(RpgCharacter observee)
		{
			m_observee = observee;
		}

		@Override
		public void leaveWorld()
		{
			m_includedEntities.remove(m_observee);
			m_observee.removeObserver(this);
		}

		@Override
		public void enterWorld()
		{
		}

		@Override
		public void flagSet(String name, int value) { }

		@Override
		public void flagCleared(String name) { }
	}
	
	public static class AreaTriggerDeclaration implements ISerializable
	{
		public float width = 1;
		public float height = 1;
		public String behavior;
		
		public IImmutableVariable arguments = new NullVariable();
		
		public AreaTriggerDeclaration() { }
		
		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("width").setValue(this.width);
			target.addChild("height").setValue(this.height);
			
			if(arguments.getChildren().length > 0)
				target.addChild("arguments").setValue(arguments);
			
			if(behavior != null)
				target.addChild("behavior").setValue(behavior);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				if(source.childExists("width"))
					this.width = source.getChild("width").getValue(Double.class).floatValue();

				if(source.childExists("height"))
					this.height = source.getChild("height").getValue(Double.class).floatValue();
				
				if(source.childExists("behavior"))
					this.behavior = source.getChild("behavior").getValue(String.class);
				
				if(source.childExists("arguments"))
					this.arguments = source.getChild("arguments");
				
			} catch(NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
		}
	}
}
