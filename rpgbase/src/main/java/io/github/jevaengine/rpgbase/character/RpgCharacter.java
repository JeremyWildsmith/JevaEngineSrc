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
package io.github.jevaengine.rpgbase.character;

import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.NoSuchChildVariableException;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.rpgbase.IImmutableItemStore;
import io.github.jevaengine.rpgbase.IImmutableLoadout;
import io.github.jevaengine.rpgbase.IItemStore;
import io.github.jevaengine.rpgbase.ILoadout;
import io.github.jevaengine.rpgbase.character.AttackTask.IAttackHandler;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession;
import io.github.jevaengine.rpgbase.dialogue.IDialogueListenerSession;
import io.github.jevaengine.rpgbase.dialogue.IDialogueRoute;
import io.github.jevaengine.rpgbase.dialogue.IDialogueRouteFactory;
import io.github.jevaengine.rpgbase.dialogue.IDialogueRouteFactory.DialogueRouteConstructionException;
import io.github.jevaengine.rpgbase.dialogue.IDialogueSpeakerSession;
import io.github.jevaengine.script.IFunctionFactory;
import io.github.jevaengine.script.IScriptFactory;
import io.github.jevaengine.script.ScriptDelegate;
import io.github.jevaengine.script.ScriptEvent;
import io.github.jevaengine.script.ScriptExecuteException;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.entity.Actor;
import io.github.jevaengine.world.entity.DefaultEntityTaskModelFactory;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.IEntityModelMapperFactory;
import io.github.jevaengine.world.entity.IEntityModelMapperFactory.UnsupportedSubjectException;
import io.github.jevaengine.world.entity.IEntityModelMapping;
import io.github.jevaengine.world.entity.NullEntityModelMapping;
import io.github.jevaengine.world.entity.tasks.MovementTask;
import io.github.jevaengine.world.entity.tasks.WonderTask;
import io.github.jevaengine.world.pathfinding.AStarRouteFactory;
import io.github.jevaengine.world.pathfinding.DefaultRoutingRules;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;
import io.github.jevaengine.world.steering.VelocityLimitSteeringDriverFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RpgCharacter extends Actor
{
	private String m_name;
	private int m_maxHealth;
	private float m_visibility;
	private float m_viewDistance;
	private float m_fieldOfView;
	private float m_visualAcuity;
	private float m_speed;
	
	private CharacterAllegiance m_allegiance;

	private IItemStore m_inventory;
	private ILoadout m_loadout;
	
	private int m_health;

	private Observers m_observers = new Observers();

	private final RpgCharacterBridge m_bridge;
	
	private final IDialogueRouteFactory m_dialogueRouteFactory;
	
	private final Logger m_logger = LoggerFactory.getLogger(RpgCharacter.class);
	
	private IEntityModelMapping m_modelMapping = new NullEntityModelMapping();
	
	public <T extends RpgCharacterBridge> RpgCharacter(IScriptFactory scriptFactory, IEntityModelMapperFactory modelMappingFactory, ISceneModelFactory modelFactory, IDialogueRouteFactory dialogueFactory, @Nullable String name, IImmutableVariable root, T entityContext) throws ValueSerializationException
	{
		super(new DefaultEntityTaskModelFactory(), scriptFactory, modelFactory, name, root, entityContext);
		m_bridge = entityContext;
		m_dialogueRouteFactory = dialogueFactory;
		init(modelMappingFactory);
	}

	public RpgCharacter(IScriptFactory scriptFactory, IAudioClipFactory audioClipFactory,
						ISceneModelFactory modelFactory, IEntityModelMapperFactory modelMapperFactory, IDialogueRouteFactory dialogueFactory,
						@Nullable String name, IImmutableVariable root) throws ValueSerializationException
	{
		this(scriptFactory, modelMapperFactory, modelFactory, dialogueFactory, name, root, new RpgCharacterBridge(audioClipFactory, scriptFactory.getFunctionFactory()));
	}
	
	public RpgCharacter(IScriptFactory scriptFactory, IAudioClipFactory audioClipFactory,
			ISceneModelFactory modelFactory, IEntityModelMapperFactory modelMapperFactory, IDialogueRouteFactory dialogueFactory,
			IImmutableVariable root) throws ValueSerializationException
	{
		this(scriptFactory, modelMapperFactory, modelFactory, dialogueFactory, null, root, new RpgCharacterBridge(audioClipFactory, scriptFactory.getFunctionFactory()));
	}
	
	private void init(IEntityModelMapperFactory modelMappingFactory) throws ValueSerializationException
	{
		RpgCharacterDeclaration decl = getConfiguration().getValue(RpgCharacterDeclaration.class);
		
		m_name = decl.name;
		m_allegiance = decl.allegiance;
		m_visibility = decl.visibility;
		m_viewDistance = decl.viewDistance;
		m_fieldOfView = decl.fieldOfView;
		m_visualAcuity = decl.visualAcuity;
		m_speed = decl.speed;
		m_maxHealth = decl.health;
		
		m_health = m_maxHealth;

		m_inventory = new CharacterInventory(decl.inventorySize);
		m_loadout = new CharacterLoadout();
		
		addConditionObserver(new BridgeNotifier());
		
		try
		{
			m_modelMapping = modelMappingFactory.createMapping(getMutableModel(), this);
		} catch(UnsupportedSubjectException e)
		{
			m_logger.error("Failed to map character model for " + getInstanceName() + ".", e);
		}
	}

	public void addActionObserver(IActionObserver observer)
	{
		m_observers.add(observer);
	}

	public void addConditionObserver(IConditionObserver observer)
	{
		m_observers.add(observer);
	}
	
	public void removeActionObserver(IActionObserver observer)
	{
		m_observers.remove(observer);
	}
	
	public void removeConditionObserver(IConditionObserver observer)
	{
		m_observers.remove(observer);
	}
	
	public String getCharacterName()
	{
		return m_name;
	}
	
	public CharacterAllegiance getAllegiance()
	{
		return m_allegiance;
	}

	public int getHealth()
	{
		return m_health;
	}

	public void setHealth(int health)
	{
		if (health != m_health)
		{
			if (health == 0)
				getTaskModel().cancelTasks();
			
			int prevHealth = m_health;
			
			m_health = Math.min(m_maxHealth, Math.max(0, health));

			m_observers.healthChanged(m_health - prevHealth);
		}
	}

	public int getMaxHealth()
	{
		return m_maxHealth;
	}

	public boolean isDead()
	{
		return m_health == 0;
	}

	public IImmutableItemStore getInventory()
	{
		return m_inventory;
	}
	
	public IImmutableLoadout getLoadout()
	{
		return m_loadout;
	}

	@Override
	public float getVisibilityFactor()
	{
		return m_visibility;
	}

	@Override
	public float getViewDistance()
	{
		return m_viewDistance;
	}

	@Override
	public float getFieldOfView()
	{
		return m_fieldOfView;
	}

	@Override
	public float getVisualAcuity()
	{
		return m_visualAcuity;
	}
	
	@Override
	public void doLogic(int deltaTime)
	{
		m_modelMapping.update(deltaTime);
	}
	
	public ILoadout getLoadOut()
	{
		return m_loadout;
	}
	
	public void wonder(int radius)
	{
		getTaskModel().addTask(new WonderTask(new VelocityLimitSteeringDriverFactory(m_speed), new AStarRouteFactory(), new DefaultRoutingRules(Direction.ALL_DIRECTIONS), radius));
	}
	
	public void moveTo(Vector3F location, float arrivalTolorance)
	{
		getTaskModel().addTask(new MovementTask(new VelocityLimitSteeringDriverFactory(m_speed), new AStarRouteFactory(), new DefaultRoutingRules(Direction.ALL_DIRECTIONS), location.getXy(), arrivalTolorance));
	}

	public void attack(RpgCharacter character, int attackPeriod)
	{
		getTaskModel().addTask(new AttackTask(character, attackPeriod, new IAttackHandler() {
			@Override
			public void doAttack(final RpgCharacter attackee)
			{
				boolean attacked = false;
				try
				{
					if(m_bridge.doAttack.hasHandler())
						attacked = m_bridge.doAttack.fire(attackee.getBridge());
					} catch (ScriptExecuteException e)
					{
						m_logger.error("doAttack script handler failed on entity " + getInstanceName(), e);
					}
					
					if(attacked)
					{
						m_observers.attackedTarget(attackee);
						attackee.m_observers.attackedBy(RpgCharacter.this);
					}
				}
			}));
	}
	
	public void speakTo(RpgCharacter listener, String dialogueRoute)
	{
		try
		{
			IDialogueRoute route = m_dialogueRouteFactory.create(dialogueRoute);
			DialogueSession session = route.begin(this, listener);
			
			if(session.isActive())
			{
				m_observers.speaking(session);
				listener.m_observers.listening(session);
			}
		} catch (DialogueRouteConstructionException e)
		{
			m_logger.error("Cannot construct dialogue session for conversation of " + getInstanceName() + " speaking to " + listener.getInstanceName(), e);
		}
	}

	
	private class BridgeNotifier implements IConditionObserver
	{
		@Override
		public void healthChanged(int delta)
		{
			try
			{
				m_bridge.onHealthChanged.fire(m_health);
			} catch (ScriptExecuteException e)
			{
				m_logger.error("onHealthChanged delegate failed on entity " + getInstanceName(), e);
			}
		}

		@Override
		public void attackedBy(RpgCharacter attacker)
		{
			try
			{
				m_bridge.onAttacked.fire(attacker.getBridge());
			} catch (ScriptExecuteException e)
			{
				m_logger.error("onAttacked delegate failed on entity " + getInstanceName(), e);
			}
		}
	}

	public interface IConditionObserver
	{
		void healthChanged(int delta);
		void attackedBy(RpgCharacter attacker);
	}
	
	public interface IActionObserver
	{
		void attackedTarget(@Nullable RpgCharacter attackee);
		void listening(IDialogueListenerSession session);
		void speaking(IDialogueSpeakerSession session);
	}
	
	private class Observers
	{
		private StaticSet<IConditionObserver> m_conditionObservers = new StaticSet<IConditionObserver>();
		private StaticSet<IActionObserver> m_actionObservers = new StaticSet<IActionObserver>();

		public void add(IConditionObserver observer)
		{
			m_conditionObservers.add(observer);
		}
		
		public void add(IActionObserver observer)
		{
			m_actionObservers.add(observer);
		}
		
		public void remove(IConditionObserver observer)
		{
			m_conditionObservers.remove(observer);
		}
		
		public void remove(IActionObserver observer)
		{
			m_actionObservers.remove(observer);
		}

		public void attackedBy(RpgCharacter attacker)
		{
			for (IConditionObserver observer : m_conditionObservers)
				observer.attackedBy(attacker);
		}

		public void healthChanged(int delta)
		{
			for (IConditionObserver observer : m_conditionObservers)
				observer.healthChanged(delta);
		}
		
		public void attackedTarget(RpgCharacter attackee)
		{
			for (IActionObserver observer : m_actionObservers)
				observer.attackedTarget(attackee);
		}
		
		public void speaking(IDialogueSpeakerSession session)
		{
			for(IActionObserver o : m_actionObservers)
				o.speaking(session);
		}
		
		public void listening(IDialogueListenerSession session)
		{
			for(IActionObserver o : m_actionObservers)
				o.listening(session);
		}
	}
	
	public static class RpgCharacterBridge extends ActorBridge
	{
		private final Logger m_logger = LoggerFactory.getLogger(RpgCharacterBridge.class);
		
		public final ScriptEvent onHealthChanged;
		public final ScriptEvent onAttacked;
		public final ScriptDelegate<Boolean> doAttack;
		
		public RpgCharacterBridge(IAudioClipFactory audioClipFactory, IFunctionFactory functionFactory)
		{
			super(audioClipFactory, functionFactory);
			
			onHealthChanged = new ScriptEvent(functionFactory);
			onAttacked = new ScriptEvent(functionFactory);
			doAttack = new ScriptDelegate<>(functionFactory, Boolean.class);
		}
		
		public final void wonder(int radius)
		{
			((RpgCharacter)getEntity()).wonder(radius);
		}

		public final void moveTo(Vector3F location, float arrivalTolorance)
		{
			((RpgCharacter)getEntity()).moveTo(new Vector3F(location.x, location.y, getEntity().getBody().getLocation().z), arrivalTolorance);
		}
		
		public final void moveTo(Vector3F v)
		{
			moveTo(v, 0.1F);
		}
		
		public final void moveTo(float x, float y)
		{
			moveTo(new Vector3F(x, y));
		}
		
		public final void moveTo(float x, float y, float tolorance)
		{
			moveTo(new Vector3F(x, y), tolorance);
		}
				
		public final void attack(IEntityBridge entity, int attackPeriod)
		{
			IEntity rawEntity = entity.getEntity();
			
			if (rawEntity instanceof RpgCharacter)
			{
				RpgCharacter character = (RpgCharacter)rawEntity;

				((RpgCharacter)getEntity()).attack(character, attackPeriod);
			}
			else
				m_logger.error("Entity " + getEntity().getInstanceName() + " cannot attack non-character entity " + entity.getName());
		}

		public final boolean isConflictingAllegiance(IEntityBridge entity)
		{
			IEntity rawEntity = entity.getEntity();
			
			if (rawEntity instanceof RpgCharacter)
			{
				RpgCharacter character = (RpgCharacter)rawEntity;
				return character.getAllegiance().conflictsWith(((RpgCharacter)getEntity()).getAllegiance());
			}
			else
			{
				m_logger.error("Entity " + getEntity().getInstanceName() + " cannot conflict in allegience with non-character entity " + entity.getName() +". Defaulting to no conflict.");
				return false;
			}
		}

		public final int getHealth()
		{
			return ((RpgCharacter) getEntity()).getHealth();
		}

		public final void setHealth(int health)
		{
			((RpgCharacter) getEntity()).setHealth(Math.max(Math.min(((RpgCharacter)getEntity()).getMaxHealth(), health), 0));
		}
		
		public final void speakTo(IEntityBridge entity, String dialogueRoute)
		{
			IEntity rawEntity = entity.getEntity();
			
			if (rawEntity instanceof RpgCharacter)
			{
				RpgCharacter character = (RpgCharacter)rawEntity;
				((RpgCharacter)getEntity()).speakTo(character, dialogueRoute);
				
			}
			else
				m_logger.error("Entity " + getEntity().getInstanceName() + " cannot speak to non-character entity " + entity.getName());
		}
	}
	
	public static class RpgCharacterDeclaration extends DefaultEntityDeclaration
	{
		public String name;
		public String model;
		public CharacterAllegiance allegiance;
		public int health;
		public int inventorySize;
		public float visibility;
		public float viewDistance;
		public float fieldOfView;
		public float visualAcuity;
		public float speed;

		@Override
		public void serialize(IVariable target) throws ValueSerializationException
		{
			target.addChild("name").setValue(name);
			target.addChild("model").setValue(model);
			target.addChild("allegiance").setValue(allegiance.ordinal());
			target.addChild("health").setValue(health);
			target.addChild("inventorySize").setValue(inventorySize);
			target.addChild("visibility").setValue(visibility);
			target.addChild("viewDistance").setValue(viewDistance);
			target.addChild("fieldOfView").setValue(fieldOfView);
			target.addChild("visualAcuity").setValue(visualAcuity);
			target.addChild("speed").setValue(speed);
			
			super.serialize(target);
		}

		@Override
		public void deserialize(IImmutableVariable source) throws ValueSerializationException
		{
			try
			{
				name = source.getChild("name").getValue(String.class);
				model = source.getChild("model").getValue(String.class);
				allegiance = CharacterAllegiance.values()[source.getChild("allegiance").getValue(Integer.class)];
				health = source.getChild("health").getValue(Integer.class);
				inventorySize = source.getChild("inventorySize").getValue(Integer.class);
				visibility = source.getChild("visibility").getValue(Double.class).floatValue();
				viewDistance = source.getChild("viewDistance").getValue(Double.class).floatValue();
				fieldOfView = source.getChild("fieldOfView").getValue(Double.class).floatValue();
				visualAcuity = source.getChild("visualAcuity").getValue(Double.class).floatValue();
				speed = source.getChild("speed").getValue(Double.class).floatValue();	
			} catch (NoSuchChildVariableException e)
			{
				throw new ValueSerializationException(e);
			}
			
			super.deserialize(source);
		}
	}
}
