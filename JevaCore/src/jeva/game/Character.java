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
package jeva.game;

import com.sun.istack.internal.Nullable;

import proguard.annotation.KeepClassMemberNames;
import jeva.Core;
import jeva.IResourceLibrary;
import jeva.config.Variable;
import jeva.config.VariableStore;
import jeva.graphics.AnimationState;
import jeva.graphics.IRenderable;
import jeva.graphics.Sprite;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.util.StaticSet;
import jeva.world.Actor;
import jeva.world.ITask;
import jeva.world.TraverseRouteTask;
import jeva.world.World;
import jeva.world.WorldDirection;
import jeva.world.TraverseRouteTask.IRouteTraveler;

/**
 * The Class Character.
 */
public abstract class Character extends Actor
{

	/** The m_f visibility factor. */
	private float m_fVisibilityFactor;

	/** The m_f view distance. */
	private float m_fViewDistance;

	/** The m_f field of view. */
	private float m_fFieldOfView;

	/** The m_f visual acuity. */
	private float m_fVisualAcuity;

	/** The m_f speed. */
	private float m_fSpeed;

	/** The m_animator. */
	private CharacterAnimator m_animator;

	/** The m_observers. */
	private Observers m_observers = new Observers();

	/**
	 * Instantiates a new character.
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
	public <Y extends Character, T extends CharacterBridge<Y>> Character(@Nullable String name, Variable root, T entityContext)
	{
		super(name, root, entityContext);

		init();
	}

	/**
	 * Inits the.
	 */
	private void init()
	{
		Variable entityVar = getEntityVariables();

		m_animator = new CharacterAnimator();

		if (entityVar.variableExists("sprite"))
		{
			Sprite sprite = Sprite.create(VariableStore.create(Core.getService(IResourceLibrary.class).openResourceStream(entityVar.getVariable("sprite").getValue().getString())));

			m_animator.m_sprite = sprite;
		}

		setDirection(WorldDirection.values()[(int) Math.round(Math.random() * WorldDirection.Zero.ordinal())]);

		m_fVisibilityFactor = entityVar.getVariable("visibilityFactor").getValue().getFloat();
		m_fViewDistance = entityVar.getVariable("viewDistance").getValue().getFloat();
		m_fFieldOfView = entityVar.getVariable("fieldOfView").getValue().getFloat();
		m_fVisualAcuity = entityVar.getVariable("visualAcuity").getValue().getFloat();
		m_fSpeed = entityVar.getVariable("speed").getValue().getFloat();
	}

	/**
	 * Gets the animator.
	 * 
	 * @return the animator
	 */
	protected CharacterAnimator getAnimator()
	{
		return m_animator;
	}

	/**
	 * Adds the observer.
	 * 
	 * @param observer
	 *            the observer
	 */
	public final void addObserver(ICharacterObserver observer)
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
	public final void removeObserver(ICharacterObserver observer)
	{
		m_observers.remove(observer);
		super.removeObserver(observer);
	}

	/**
	 * Creates the move task.
	 * 
	 * @param dest
	 *            the dest
	 * @param fRadius
	 *            the f radius
	 * @return the traverse route task
	 */
	private TraverseRouteTask createMoveTask(final Vector2D dest, final float fRadius)
	{
		return new TraverseRouteTask(new CharacterRouteTraveler(), dest, fRadius);
	}

	/**
	 * Creates the move to task.
	 * 
	 * @param dest
	 *            the dest
	 * @param fRadius
	 *            the f radius
	 * @return the i task
	 */
	protected final ITask createMoveToTask(Vector2D dest, float fRadius)
	{
		return createMoveTask(dest, fRadius);
	}

	/**
	 * Creates the wonder task.
	 * 
	 * @param fRadius
	 *            the f radius
	 * @return the i task
	 */
	protected final ITask createWonderTask(float fRadius)
	{
		return createMoveTask(null, fRadius);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Entity#doLogic(int)
	 */
	@Override
	public void doLogic(int deltaTime)
	{
		m_animator.update(deltaTime);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getVisibilityFactor()
	 */
	@Override
	public float getVisibilityFactor()
	{
		return m_fVisibilityFactor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getViewDistance()
	 */
	@Override
	public float getViewDistance()
	{
		return m_fViewDistance;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getFieldOfView()
	 */
	@Override
	public float getFieldOfView()
	{
		return m_fFieldOfView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getVisualAcuity()
	 */
	@Override
	public float getVisualAcuity()
	{
		return m_fVisualAcuity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getSpeed()
	 */
	@Override
	public float getSpeed()
	{
		return m_fSpeed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getAllowedMovements()
	 */
	@Override
	public WorldDirection[] getAllowedMovements()
	{
		return WorldDirection.ALL_MOVEMENT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getGraphics()
	 */
	@Override
	public IRenderable[] getGraphics()
	{
		if (m_animator.m_sprite != null)
			return new IRenderable[]
			{ m_animator.m_sprite };
		else
			return new IRenderable[] {};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getTileWidth()
	 */
	@Override
	public int getTileWidth()
	{
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.Actor#getTileHeight()
	 */
	@Override
	public int getTileHeight()
	{
		return 1;
	}

	/**
	 * The Class CharacterRouteTraveler.
	 */
	protected final class CharacterRouteTraveler implements IRouteTraveler
	{

		/**
		 * Instantiates a new character route traveler.
		 */
		public CharacterRouteTraveler()
		{

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jeva.world.MovementTask.ITraveler#updateMovement(jeva.math.Vector2F)
		 */
		@Override
		public void updateMovement(Vector2F delta)
		{
			WorldDirection direction = WorldDirection.fromVector(delta);

			if (direction != WorldDirection.Zero)
				setDirection(direction);

			if (delta.isZero())
			{
				m_observers.movingTowards(null);
			} else
			{
				move(delta);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jeva.world.MovementTask.ITraveler#setDestination(jeva.math.Vector2F)
		 */
		@Override
		public void setDestination(Vector2F target)
		{
			m_observers.movingTowards(target);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.MovementTask.ITraveler#getWorld()
		 */
		@Override
		public World getWorld()
		{
			return Character.this.getWorld();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.MovementTask.ITraveler#getLocation()
		 */
		@Override
		public Vector2F getLocation()
		{
			return Character.this.getLocation();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see jeva.world.MovementTask.ITraveler#getSpeed()
		 */
		@Override
		public float getSpeed()
		{
			return Character.this.getSpeed();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * jeva.world.TraverseRouteTask.IRouteTraveler#getAllowedMovements()
		 */
		@Override
		public WorldDirection[] getAllowedMovements()
		{
			return Character.this.getAllowedMovements();
		}
	}

	/**
	 * The Class CharacterAnimator.
	 */
	protected class CharacterAnimator
	{

		/** The m_sprite. */
		private Sprite m_sprite;

		/** The m_animation. */
		private String m_animation;

		/** The m_state. */
		private AnimationState m_state;

		/** The m_override animator. */
		private CharacterAnimator m_overrideAnimator;

		/**
		 * Instantiates a new character animator.
		 * 
		 * @param animator
		 *            the animator
		 */
		public CharacterAnimator(CharacterAnimator animator)
		{
			m_overrideAnimator = animator;
		}

		/**
		 * Update.
		 * 
		 * @param deltaTime
		 *            the delta time
		 */
		public final void update(int deltaTime)
		{
			if (m_sprite != null)
				m_sprite.update(deltaTime);
		}

		/**
		 * Instantiates a new character animator.
		 */
		private CharacterAnimator()
		{
			m_state = AnimationState.Play;
		}

		/**
		 * Update sprite.
		 */
		private void updateSprite()
		{
			if (m_overrideAnimator == null)
			{
				if (m_sprite != null)
					m_sprite.setAnimation(m_animation, m_state);
			} else
				m_overrideAnimator.updateSprite();
		}

		/**
		 * Sets the animation.
		 * 
		 * @param animation
		 *            the animation
		 * @param playOnce
		 *            the play once
		 */
		protected final void setAnimation(String animation, boolean playOnce)
		{
			if (m_overrideAnimator == null)
			{
				AnimationState state = playOnce ? AnimationState.PlayToEnd : AnimationState.Play;

				if (m_animation != null && m_animation.compareTo(animation) == 0 && m_state == state)
					return;

				m_animation = animation;
				m_state = state;
				updateSprite();
			} else
				m_overrideAnimator.setAnimation(animation, playOnce);
		}

		/**
		 * Sets the animation.
		 * 
		 * @param animation
		 *            the new animation
		 */
		protected final void setAnimation(String animation)
		{
			setAnimation(animation, false);
		}
	}

	/**
	 * An asynchronous update interface for receiving notifications about
	 * ICharacter information as the ICharacter is constructed.
	 */
	public interface ICharacterObserver extends IActorObserver
	{
		/**
		 * This method is called when information about an ICharacter which was
		 * previously requested using an asynchronous interface becomes
		 * available.
		 * 
		 * @param target
		 *            the target
		 */
		void movingTowards(@Nullable Vector2F target);
	}

	/**
	 * The Class Observers.
	 */
	private static class Observers extends StaticSet<ICharacterObserver>
	{
		/**
		 * Moving towards.
		 * 
		 * @param target
		 *            the target
		 */
		public void movingTowards(@Nullable Vector2F target)
		{
			for (ICharacterObserver observer : this)
				observer.movingTowards(target);
		}
	}

	/**
	 * The Class CharacterBridge.
	 * 
	 * @param <A>
	 *            the generic type
	 */
	@KeepClassMemberNames
	public static class CharacterBridge<A extends Character> extends ActorBridge<A>
	{
		/**
		 * Wonder.
		 * 
		 * @param fRadius
		 *            the f radius
		 */
		public final void wonder(float fRadius)
		{
			getMe().addTask(((Character) getMe()).createMoveTask(null, fRadius));
		}

		/**
		 * Move to.
		 * 
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param fRadius
		 *            the f radius
		 */
		public final void moveTo(int x, int y, float fRadius)
		{
			getMe().addTask(((Character) getMe()).createMoveTask(new Vector2D(x, y), fRadius));
		}

		/**
		 * Move to.
		 * 
		 * @param x
		 *            the x
		 * @param y
		 *            the y
		 * @param fRadius
		 *            the f radius
		 * @param maxSteps
		 *            the max steps
		 */
		public final void moveTo(int x, int y, float fRadius, int maxSteps)
		{
			TraverseRouteTask moveTask = ((Character) getMe()).createMoveTask(new Vector2D(x, y), fRadius);
			moveTask.truncate(maxSteps);

			getMe().addTask(moveTask);
		}
	}
}
