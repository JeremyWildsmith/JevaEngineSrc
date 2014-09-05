package io.github.jevaengine.world.scene.model.sprite;

import io.github.jevaengine.graphics.Animation.IAnimationEventListener;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.Sprite.NoSuchSpriteAnimation;
import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelAnimation.SceneModelAnimationState;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelComponent;

import java.awt.Graphics2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DefaultSceneModelComponent implements ISceneModelComponent
{
	private final Logger m_logger = LoggerFactory.getLogger(DefaultSceneModelComponent.class);
	private final Sprite m_sprite;
	private final String m_animation;
	private final Rect3F m_bounds;
	
	private SceneModelAnimationState m_state = SceneModelAnimationState.Stop;
	
	private final Observers m_observers = new Observers();
	
	DefaultSceneModelComponent(DefaultSceneModelComponent source)
	{
		m_sprite = new Sprite(source.m_sprite);
		m_animation = source.m_animation;
		m_bounds = new Rect3F(source.m_bounds);
		m_state = source.m_state;
	}
	
	DefaultSceneModelComponent(Sprite sprite, String animation, Rect3F bounds)
	{
		m_sprite = sprite;
		m_animation = animation;
		m_bounds = bounds;
	}
	
	void update(int delta)
	{
		m_sprite.update(delta);
	}

	SceneModelAnimationState getState()
	{
		return m_state;
	}
	
	void setState(SceneModelAnimationState state)
	{
		try
		{
			m_state = state;
			
			switch(state)
			{
			case Play:
				m_sprite.setAnimation(m_animation, AnimationState.Play, new IAnimationEventListener() {
					@Override
					public void onFrameEvent(String name) {
						m_observers.onFrameEvent(name);
					}
					
					@Override
					public void onStateEvent() { }
				});
				
				break;
			case PlayToEnd:
				m_sprite.setAnimation(m_animation, AnimationState.PlayToEnd, new IAnimationEventListener() {
					@Override
					public void onFrameEvent(String name) {
						m_observers.onFrameEvent(name);
					}
					
					@Override
					public void onStateEvent()
					{
						m_state = SceneModelAnimationState.Stop;
					}
				});
				break;
			case PlayWrap:
				m_sprite.setAnimation(m_animation, AnimationState.PlayWrap, new IAnimationEventListener() {
					@Override
					public void onFrameEvent(String name) {
						m_observers.onFrameEvent(name);
					}
					
					@Override
					public void onStateEvent() { }
				});
				break;
			case Stop:
				m_sprite.setAnimation(m_animation, AnimationState.Stop);
				break;
			default:
				assert false: "Unexpected SceneModelAnimationState";
				break;
			}
		} catch (NoSuchSpriteAnimation e)
		{
			m_logger.error("Unable to set model component animation as it does not exist for given direction.", e);
		}
	}
	
	void addObserver(IDefaultSceneModelComponentObserver o)
	{
		m_observers.add(o);
	}
	
	void removeObserver(IDefaultSceneModelComponentObserver o)
	{
		m_observers.remove(o);
	}
	
	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		m_sprite.render(g, x, y, scale);
	}

	@Override
	public boolean testPick(int x, int y, float scale)
	{
		return m_sprite.testPick(x, y, scale);
	}

	@Override
	public Rect3F getBounds()
	{
		return new Rect3F(m_bounds);
	}
	
	public interface IDefaultSceneModelComponentObserver
	{
		void onFrameEvent(String name);
	}
	
	private static final class Observers extends StaticSet<IDefaultSceneModelComponentObserver>
	{
		public void onFrameEvent(String name)
		{
			for(IDefaultSceneModelComponentObserver o : this)
				o.onFrameEvent(name);
		}
	}
}
