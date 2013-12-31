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

import io.github.jevaengine.Core;
import io.github.jevaengine.CoreScriptException;
import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.ParticleEmitter;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.rpgbase.Inventory.InventoryBridge;
import io.github.jevaengine.rpgbase.Item.ItemType;
import io.github.jevaengine.rpgbase.ui.StatisticGuage;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Actor;
import io.github.jevaengine.world.EffectMap;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.MovementTask;
import io.github.jevaengine.world.TraverseRouteTask;
import io.github.jevaengine.world.WorldDirection;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.script.ScriptException;

public final class RpgCharacter extends Actor
{
	private CharacterAllegiance m_allegiance;
	
	private String m_name;
	private int m_maxHealth;
	private float m_visibility;
	private float m_viewDistance;
	private float m_fieldOfView;
	private float m_visualAcuity;
	private float m_speed;

	private Inventory m_inventory;
	private Loadout m_loadout;
	
	private int m_health;
	
	private ParticleEmitter m_bloodEmitter;

	private Observers m_observers = new Observers();

	private CharacterModel m_model;
	private RpgCharacterScript m_script = new RpgCharacterScript();

	private RpgCharacterTaskFactory m_taskFactory;
	
	public <T extends RpgCharacterBridge> RpgCharacter(@Nullable String name, IVariable root, T entityContext, @Nullable RpgCharacterTaskFactory taskFactory)
	{
		super(name, root, entityContext);
		m_taskFactory = (taskFactory == null ? new RpgCharacterTaskFactory() : taskFactory);
		
		init();
	}

	public RpgCharacter(@Nullable String name, IVariable root, @Nullable RpgCharacterTaskFactory taskFactory)
	{
		super(name, root, new RpgCharacterBridge());
		m_taskFactory = (taskFactory == null ? new RpgCharacterTaskFactory() : taskFactory);
		
		init();
	}
	
	public <T extends RpgCharacterBridge> RpgCharacter(@Nullable String name, IVariable root, T entityContext)
	{
		this(name, root, entityContext, null);
	}
	
	
	public RpgCharacter(@Nullable String name, IVariable root)
	{
		this(name, root, new RpgCharacterBridge());
	}
	
	public <T extends RpgCharacterBridge> RpgCharacter(IVariable root, T entityContext, @Nullable RpgCharacterTaskFactory taskFactory)
	{
		this(null, root, entityContext, taskFactory);
	}
	
	public <T extends RpgCharacterBridge> RpgCharacter(IVariable root, T entityContext)
	{
		this(null, root, entityContext, null);
	}
	
	public RpgCharacter(IVariable root, RpgCharacterTaskFactory factory)
	{
		this(null, root, factory);
	}
	
	public RpgCharacter(IVariable root)
	{
		this(null, root);
	}

	private void init()
	{
		RpgCharacterDeclaration decl = getConfiguration().getValue(RpgCharacterDeclaration.class);
		
		setDirection(WorldDirection.values()[(int)Math.round(Math.random() * (float)WorldDirection.Zero.ordinal())]);
		
		m_bloodEmitter = ParticleEmitter.create(Core.getService(IResourceLibrary.class).openConfiguration(decl.blood));
		m_name = decl.name;
		m_allegiance = decl.allegiance;
		m_visibility = decl.visibility;
		m_viewDistance = decl.viewDistance;
		m_fieldOfView = decl.fieldOfView;
		m_visualAcuity = decl.visualAcuity;
		m_speed = decl.speed;
		m_maxHealth = decl.health;
		
		m_health = m_maxHealth;

		m_inventory = new Inventory(this, decl.inventorySize);
		m_loadout = new Loadout();
		
		m_model = new CharacterModel(Sprite.create(Core.getService(IResourceLibrary.class).openConfiguration(decl.sprite)));
		addActionObserver(m_model);
		addConditionObserver(m_model);
		addConditionObserver(m_script);
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
	
	@Override
	public IRenderable getGraphic()
	{
		return m_model;
	}

	@Override
	public WorldDirection[] getAllowedMovements()
	{
		return WorldDirection.ALL_MOVEMENT;
	}

	@Override
	public int getTileWidth()
	{
		return 1;
	}

	@Override
	public int getTileHeight()
	{
		return 1;
	}

	public final CharacterAllegiance getAllegiance()
	{
		return m_allegiance;
	}

	public final int getHealth()
	{
		return m_health;
	}

	public final void setHealth(int health)
	{
		if (health != m_health)
		{
			if (health == 0)
				cancelTasks();
			
			int prevHealth = m_health;
			
			m_health = Math.min(m_maxHealth, Math.max(0, health));

			m_observers.healthChanged(m_health - prevHealth);
		}
	}

	public final int getMaxHealth()
	{
		return m_maxHealth;
	}

	public final boolean isDead()
	{
		return m_health == 0;
	}

	public final Inventory getInventory()
	{
		return m_inventory;
	}
	
	public final Loadout getLoadout()
	{
		return m_loadout;
	}

	@Override
	public final float getVisibilityFactor()
	{
		return m_visibility;
	}

	@Override
	public final float getViewDistance()
	{
		return m_viewDistance;
	}

	@Override
	public final float getFieldOfView()
	{
		return m_fieldOfView;
	}

	@Override
	public final float getVisualAcuity()
	{
		return m_visualAcuity;
	}

	@Override
	public final float getSpeed()
	{
		return m_speed;
	}
	
	private MovementTask createMoveTask(@Nullable Vector2F dest, float fRadius)
	{
		MovementTask task = m_taskFactory.createMovementTask(this, dest, fRadius);
		task.addObserver(m_observers);
		
		return task;
	}
	
	private AttackTask createAttackTask(final @Nullable RpgCharacter target)
	{
		AttackTask task = m_taskFactory.createAttackTask(this, target);
		task.addObserver(m_observers);
		
		return task;
	}
	
	public void moveTo(Vector2F destination)
	{
		cancelTasks();
		addTask(createMoveTask(destination, m_speed));
	}
	
	public void loot(RpgCharacter target)
	{
		cancelTasks();
		addTask(new LootTask(this, target.getInventory()));
	}

	public void attack(@Nullable RpgCharacter target)
	{
		addTask(createAttackTask(target));
	}

	protected final MovementTask createWonderTask(float fRadius)
	{
		return createMoveTask(null, fRadius);
	}
	
	@Override
	public void blendEffectMap(EffectMap globalEffectMap)
	{
		globalEffectMap.applyOverlayEffects(getLocation().round(), new TileEffects(this.isDead()));
		globalEffectMap.applyOverlayEffects(getLocation().round(), new TileEffects(this));
	}
	
	@Override
	public void doLogic(int deltaTime)
	{
		m_model.update(deltaTime);

		m_bloodEmitter.update(deltaTime);
	}
	
	private class RpgCharacterScript implements IConditionObserver
	{
		@Override
		public void healthChanged(int delta)
		{
			if(getHealth() != 0)
				return;
			
			try
			{
				getScript().invokeScriptFunction("onDie");
			} catch (NoSuchMethodException e)
			{
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error invoking RpgCharacter onDie: " + e.toString());
			}
		}

		@Override
		public void attacked(RpgCharacter attacker)
		{
			try
			{
				getScript().invokeScriptFunction("onAttacked",
						attacker.getScriptBridge());
			} catch (NoSuchMethodException e)
			{
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error invoking RpgCharacter onAttacked: "
						+ e.toString());
			}
		}

		public boolean onAttack(RpgCharacter attackee)
		{
			try
			{
				Object o = getScript().invokeScriptFunction("onAttack", attackee.getScriptBridge());

				if (!(o instanceof Boolean))
					throw new CoreScriptException("onAttack returned unexpected value, expected a boolean");

				return ((Boolean) o).booleanValue();

			} catch (NoSuchMethodException e)
			{
				return false;
			} catch (ScriptException e)
			{
				throw new CoreScriptException("Error occured executing onAttack: " + e.toString());
			}
		}
	}

	protected final class CharacterModel implements IConditionObserver, IActionObserver, IRenderable
	{
		private static final int MOVEMENT_TIMEOUT = 150;
		
		private Sprite m_sprite;
		private String m_lastDirectionalAnimation = "idle";
		private AnimationState m_lastDirectionalState = AnimationState.Stop;

		private WorldDirection m_lastDirection;
		
		private StatisticGuage m_healthGuage = new StatisticGuage(new Vector2D(25, -10), Color.red, 50, 3, 1.0F);
		
		private int m_bleedTimeout = 0;
		private int m_showHealthTimeout = 0;
		private int m_moveTimeout = 0;
		
		private CharacterModel(Sprite sprite)
		{
			m_sprite = sprite;
			m_lastDirection = getDirection() == WorldDirection.Zero ? WorldDirection.XMinus : RpgCharacter.this.getDirection();
			idle();
		}

		private void setDirectionalAnimation(String animationName, AnimationState state, @Nullable Runnable animationEventHandler)
		{
			WorldDirection newDirection = getDirection() == WorldDirection.Zero ? m_lastDirection : getDirection();
			
			if(m_lastDirectionalAnimation == animationName &&
					m_lastDirection == newDirection &&
					m_lastDirectionalState == state)
				return;
			
			m_lastDirection = newDirection;
			
			m_lastDirectionalAnimation = animationName;
			m_lastDirectionalState = state;
			
			m_sprite.setAnimation(m_lastDirection.toString() + animationName, state, animationEventHandler);
		}
		
		private void setDirectionalAnimation(String animationName, AnimationState state)
		{
			setDirectionalAnimation(animationName, state, null);
		}

		public void updateDirectional()
		{
			if(getDirection() != m_lastDirection)
				setDirectionalAnimation(m_lastDirectionalAnimation, m_lastDirectionalState);
		}

		private void idle()
		{
			if(getHealth() == 0)
				setDirectionalAnimation("die", AnimationState.PlayToEnd);
			else
				setDirectionalAnimation("idle", AnimationState.PlayWrap);
		}

		@Override
		public void attack(RpgCharacter attackee)
		{
			WorldDirection attackDirection = WorldDirection.fromVector(attackee.getLocation().difference(RpgCharacter.this.getLocation()));
			setDirection(attackDirection);
			updateDirectional();
			
			setDirectionalAnimation("attack", AnimationState.PlayToEnd, new Runnable() {
				@Override
				public void run()
				{
					idle();
				}});
		}

		@Override
		public void healthChanged(int delta)
		{
			if(getHealth() == 0)
				setDirectionalAnimation("die", AnimationState.PlayToEnd);
			else if(m_lastDirectionalAnimation.equals("die"))
				idle();
				
			m_healthGuage.setValue((float)getHealth() / (float)getMaxHealth());
			
			m_showHealthTimeout = 3000;
			
			if(delta < 0)
				m_bleedTimeout = 650;
		}
		
		@Override
		public void beginMoving()
		{
			m_moveTimeout = 0;
			setDirectionalAnimation("walk", AnimationState.Play);
		}
		
		@Override
		public void endMoving()
		{
			m_moveTimeout = MOVEMENT_TIMEOUT;
		}
		
		public void update(int delta)
		{
			if(getDirection() != m_lastDirection)
				setDirectionalAnimation(m_lastDirectionalAnimation, m_lastDirectionalState);
			
			m_sprite.update(delta);
			
			if (m_showHealthTimeout > 0)
				m_showHealthTimeout -= delta;
			
			if (m_bleedTimeout > 0)
			{
				m_bloodEmitter.setEmit(true);
				m_bleedTimeout -= delta;
			} else
				m_bloodEmitter.setEmit(false);
			
			if(m_moveTimeout > 0 && m_lastDirectionalAnimation.equals("walk"))
			{
				m_moveTimeout -= delta;
				
				if(m_moveTimeout <= 0)
					idle();
			}
		}

		@Override
		public void render(Graphics2D g, int x, int y, float fScale)
		{
			m_sprite.render(g, x, y, fScale);
			m_bloodEmitter.render(g, x, y, fScale);

			if (m_showHealthTimeout > 0)
				m_healthGuage.render(g, x, y, fScale);
		}
		
		@Override
		public void attacked(RpgCharacter attacker) { }

		@Override
		public void movingTowards(Vector2F target) { }
	}

	public interface IConditionObserver
	{
		void healthChanged(int delta);
		void attacked(RpgCharacter attacker);
	}
	
	public interface IActionObserver
	{
		void attack(@Nullable RpgCharacter attackee);
		void movingTowards(Vector2F target);
		void beginMoving();
		void endMoving();
	}
	
	public static class RpgCharacterTaskFactory
	{
		public MovementTask createMovementTask(final RpgCharacter host, final @Nullable Vector2F dest, final float fRadius)
		{
			return new TraverseRouteTask(dest, host.getAllowedMovements(), host.getSpeed(), fRadius);
		}
		
		public AttackTask createAttackTask(final RpgCharacter host, final @Nullable RpgCharacter target)
		{
			return new AttackTask(target)
			{
				@Override
				public boolean doAttack(RpgCharacter target)
				{
					ItemSlot weapon = host.getLoadout().getSlot(ItemType.Weapon);

					boolean attacked;
					
					if(weapon != null && !weapon.isEmpty())
						attacked = weapon.getItem().use(host, target);
					else
						attacked = host.m_script.onAttack(target);
					
					return attacked;
				}
			};
		}
	}
	
	private class Observers implements MovementTask.IMovementObserver, AttackTask.IAttackObserver
	{
		private StaticSet<IConditionObserver> m_conditionObservers = new StaticSet<IConditionObserver>();
		private StaticSet<IActionObserver> m_actionObservers = new StaticSet<IActionObserver>();

		private boolean m_isMoving = false;
		
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

		public void attacked(RpgCharacter attacker)
		{
			for (IConditionObserver observer : m_conditionObservers)
				observer.attacked(attacker);
		}

		public void healthChanged(int delta)
		{
			for (IConditionObserver observer : m_conditionObservers)
				observer.healthChanged(delta);
		}
		
		@Override
		public void attack(@Nullable RpgCharacter attackee)
		{
			for (IActionObserver observer : m_actionObservers)
				observer.attack(attackee);
			
			if(attackee != null)
				attackee.m_observers.attacked(RpgCharacter.this);
		}
		
		@Override
		public void movingTowards(Vector2F target)
		{
			for (IActionObserver observer : m_actionObservers)
				observer.movingTowards(target);
		}
		
		@Override
		public void updateMovement(Vector2F delta)
		{
			WorldDirection direction = WorldDirection.fromVector(delta);

			if (direction != WorldDirection.Zero)
				setDirection(direction);

			move(delta);
			
			if(!m_isMoving && !delta.isZero())
				beginMoving();
			else if(m_isMoving && delta.isZero())
				endMoving();
			
			m_isMoving = !delta.isZero();
		}
		
		public void beginMoving()
		{
			for(IActionObserver observer : m_actionObservers)
				observer.beginMoving();
		}
		
		public void endMoving()
		{
			for (IActionObserver observer : m_actionObservers)
				observer.endMoving();
		}
	}

	public static class RpgCharacterBridge extends ActorBridge<RpgCharacter>
	{
		public void wonder(float fRadius)
		{
			getEntity().addTask(((RpgCharacter) getEntity()).createMoveTask(null, fRadius));
		}

		public void moveTo(int x, int y, float fRadius)
		{
			getEntity().addTask(((RpgCharacter) getEntity()).createMoveTask(new Vector2F(x, y), fRadius));
		}

		public void moveTo(int x, int y, float fRadius, int maxSteps)
		{
			MovementTask moveTask = ((RpgCharacter) getEntity()).createMoveTask(new Vector2F(x, y), fRadius);

			getEntity().addTask(moveTask);
		}
				
		public void attack(EntityBridge<?> entity)
		{
			if (!(entity instanceof RpgCharacterBridge))
				return;

			RpgCharacter character = ((RpgCharacterBridge) entity).getEntity();

			getEntity().attack(character);
		}

		public boolean isConflictingAllegiance(EntityBridge<?> entity)
		{
			if (!(entity instanceof RpgCharacterBridge))
				return false;

			RpgCharacter character = ((RpgCharacterBridge) entity).getEntity();

			return character.m_allegiance.conflictsWith(((RpgCharacter) getEntity()).m_allegiance);
		}
		
		public void loot(EntityBridge<?> entity)
		{
			if (!(entity instanceof RpgCharacterBridge))
				return;
			
			getEntity().addTask(new LootTask(getEntity(), ((RpgCharacterBridge)entity).getEntity().getInventory()));
		}
				
		public InventoryBridge getInventory()
		{
			return getEntity().getInventory().getScriptBridge();
		}

		public int getHealth()
		{
			return ((RpgCharacter) getEntity()).getHealth();
		}

		public void setHealth(int health)
		{
			((RpgCharacter) getEntity()).setHealth(Math.max(Math.min(getEntity().getMaxHealth(), health), 0));
		}
	}
	
	public static class RpgCharacterDeclaration extends EntityDeclaration
	{
		public String name;
		public String sprite;
		public String blood;
		public CharacterAllegiance allegiance;
		public int health;
		public int inventorySize;
		public float visibility;
		public float viewDistance;
		public float fieldOfView;
		public float visualAcuity;
		public float speed;

		@Override
		public void serialize(IVariable target)
		{
			target.addChild("name").setValue(name);
			target.addChild("sprite").setValue(sprite);
			target.addChild("blood").setValue(blood);
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
		public void deserialize(IVariable source)
		{
			name = source.getChild("name").getValue(String.class);
			sprite = source.getChild("sprite").getValue(String.class);
			blood = source.getChild("blood").getValue(String.class);
			allegiance = CharacterAllegiance.values()[source.getChild("allegiance").getValue(Integer.class)];
			health = source.getChild("health").getValue(Integer.class);
			inventorySize = source.getChild("inventorySize").getValue(Integer.class);
			visibility = source.getChild("visibility").getValue(Double.class).floatValue();
			viewDistance = source.getChild("viewDistance").getValue(Double.class).floatValue();
			fieldOfView = source.getChild("fieldOfView").getValue(Double.class).floatValue();
			visualAcuity = source.getChild("visualAcuity").getValue(Double.class).floatValue();
			speed = source.getChild("speed").getValue(Double.class).floatValue();
			
			super.deserialize(source);
		}
	}
}
