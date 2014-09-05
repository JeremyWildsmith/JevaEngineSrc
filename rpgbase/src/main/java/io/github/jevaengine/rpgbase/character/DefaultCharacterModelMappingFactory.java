package io.github.jevaengine.rpgbase.character;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.rpgbase.character.DefaultCharacterModelMappingFactory.RpgCharacterState.MovementState;
import io.github.jevaengine.rpgbase.character.RpgCharacter.IActionObserver;
import io.github.jevaengine.rpgbase.dialogue.IDialogueListenerSession;
import io.github.jevaengine.rpgbase.dialogue.IDialogueSpeakerSession;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.IEntityModelMapperFactory;
import io.github.jevaengine.world.entity.IEntityModelMapping;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelAnimation;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelAnimation.SceneModelAnimationState;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.NoSuchAnimationException;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.NullSceneModelAnimation;
import io.github.jevaengine.world.scene.model.ISceneModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultCharacterModelMappingFactory implements IEntityModelMapperFactory
{
	@Override
	public IEntityModelMapping createMapping(ISceneModel model, IEntity subject) throws UnsupportedSubjectException
	{
		if(!(subject instanceof RpgCharacter))
			throw new UnsupportedSubjectException();
		else
			return new DefaultCharacterModelMapping((RpgCharacter)subject, model);
	}
	
	public static final class RpgCharacterState implements IDisposable
	{
		private RpgCharacterState.MovementState m_movementState = MovementState.Idle;
		
		private final RpgCharacter m_subject;
		
		private boolean m_wasDamaged = false;
		private boolean m_wasAttacking = true;
		
		private int m_lastHealth = 0;
		
		private Direction m_lastDirection = Direction.XYPlus;
		
		private final IActionObserver m_actionObserver = new ActionObserver();
		
		private RpgCharacterState(RpgCharacter subject)
		{
			m_lastHealth = subject.getHealth();
			m_subject = subject;
			
			m_subject.addActionObserver(m_actionObserver);
		}
		
		@Override
		public void dispose()
		{
			m_subject.removeActionObserver(m_actionObserver);
		}
		
		public void update(int deltaTime)
		{
			Vector3F velocity = m_subject.getBody().getLinearVelocity();
			float speed = velocity.getLength();
			
			if(speed <= MovementState.Idle.getMaxSpeed())
				m_movementState = MovementState.Idle;
			else
				m_movementState = MovementState.Walking;
			
			int currentHealth = m_subject.getHealth();
			
			if(currentHealth < m_lastHealth)
				m_wasDamaged = true;
			
			m_lastHealth = currentHealth;
			
			if(!velocity.getXy().isZero())
				m_lastDirection = Direction.fromVector(velocity.getXy());
		}
		
		public Direction getDirection()
		{
			return m_lastDirection;
		}
		
		public boolean wasDamaged()
		{
			boolean old = m_wasDamaged;
			m_wasDamaged = false;
			
			return old;
		}
		
		public boolean wasAttacking()
		{
			boolean old = m_wasAttacking;
			m_wasAttacking = false;
			
			return old;
		}
		
		public int getHealth()
		{
			return m_subject.getHealth();
		}
	
		public MovementState getMovementState()
		{
			return m_movementState;
		}
		
		public static enum MovementState
		{
			Idle(0.1F),
			Walking(2.5F);
			
			private float m_maxSpeed;
			
			MovementState(float maxSpeed)
			{
				m_maxSpeed = maxSpeed;
			}
			
			public float getMaxSpeed()
			{
				return m_maxSpeed;
			}
		}
		
		private final class ActionObserver implements IActionObserver
		{
			@Override
			public void attackedTarget(RpgCharacter attackee)
			{
				m_wasAttacking = true;
			}

			@Override
			public void listening(IDialogueListenerSession session) { }

			@Override
			public void speaking(IDialogueSpeakerSession session) { }
		}
	}
	
	public static final class DefaultCharacterModelMapping implements IEntityModelMapping
	{
		private final Logger m_logger = LoggerFactory.getLogger(DefaultCharacterModelMapping.class);
		
		private RpgCharacterState m_characterState;
		
		private final ISceneModel m_sceneModel;
		private final ISceneModelAnimation m_idleAnimation;
		private final ISceneModelAnimation m_walkingAnimation;
		private final ISceneModelAnimation m_damagedAnimation;
		private final ISceneModelAnimation m_dieAnimation;
		private final ISceneModelAnimation m_attackAnimation;
		
		@Nullable
		private MovementState m_lastMovementState = null;
		
		private Direction m_lastDirection = Direction.Zero;
		
		public DefaultCharacterModelMapping(RpgCharacter host, ISceneModel sceneModel)
		{
			m_sceneModel = sceneModel;
			m_characterState = new RpgCharacterState(host);
			m_idleAnimation = getAnimation("idle", sceneModel);
			m_walkingAnimation = getAnimation("walk", sceneModel);
			m_dieAnimation = getAnimation("die", sceneModel);
			m_damagedAnimation = getAnimation("damaged", sceneModel);
			m_attackAnimation = getAnimation("attack", sceneModel);
			
			m_lastDirection = m_characterState.getDirection();
			sceneModel.setDirection(m_lastDirection);
		}
		
		@Override
		public void dispose()
		{
			m_characterState.dispose();
		}
		
		private ISceneModelAnimation getAnimation(String name, ISceneModel sceneModel)
		{
			try
			{
				return sceneModel.getAnimation(name);
			} catch (NoSuchAnimationException e)
			{
				m_logger.error("Character model mapper is missing required animation '" + name + "' using null animation as replacement.");
				return new NullSceneModelAnimation();
			}
		}
		
		@Override
		public void update(int deltaTime)
		{
			if(m_lastDirection != m_characterState.getDirection())
			{
				m_lastDirection = m_characterState.getDirection();
				m_sceneModel.setDirection(m_lastDirection);
			}
			
			m_characterState.update(deltaTime);	
			
			if(m_characterState.wasDamaged())
			{	
				if(m_characterState.getHealth() == 0)
				{
					m_dieAnimation.setState(SceneModelAnimationState.PlayToEnd);
					m_walkingAnimation.setState(SceneModelAnimationState.Stop);
					m_idleAnimation.setState(SceneModelAnimationState.Stop);
				}else
				{
					m_damagedAnimation.setState(SceneModelAnimationState.PlayToEnd);
					m_lastMovementState = null;	
				}
			}else if(m_characterState.wasAttacking() == true)
			{
				m_attackAnimation.setState(SceneModelAnimationState.PlayToEnd);
			}else if(m_characterState.getHealth() > 0)
			{
				MovementState currentMovementState = m_characterState.getMovementState();
				
				if(currentMovementState != m_lastMovementState)
				{
					m_lastMovementState = currentMovementState;
					switch(currentMovementState)
					{
						case Walking:
							m_walkingAnimation.setState(SceneModelAnimationState.Play);
							m_idleAnimation.setState(SceneModelAnimationState.Stop);
							break;
						default:
							assert false: "Unrecognized movement state.";
						case Idle:
							m_walkingAnimation.setState(SceneModelAnimationState.Stop);
							m_idleAnimation.setState(SceneModelAnimationState.Play);
					}
				}
			}
		}
	}
}
