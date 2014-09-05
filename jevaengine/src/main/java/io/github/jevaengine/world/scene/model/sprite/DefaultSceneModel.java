package io.github.jevaengine.world.scene.model.sprite;

import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelAnimation.SceneModelAnimationState;
import io.github.jevaengine.world.scene.model.ISceneModel;
import io.github.jevaengine.world.scene.model.sprite.DefaultSceneModelComponent.IDefaultSceneModelComponentObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DefaultSceneModel implements ISceneModel
{
	private final LinkedHashMap<String, DefaultSceneModelAnimation> m_animations = new LinkedHashMap<>();
	private final Observers m_observers = new Observers();
	
	private DefaultSceneModelAnimation m_currentAnimation = null;
	private Direction m_direction = Direction.XYPlus;
	
	DefaultSceneModel() { }

	DefaultSceneModel(DefaultSceneModel source)
	{
		for(Map.Entry<String, DefaultSceneModelAnimation> a : source.m_animations.entrySet())
		{
			DefaultSceneModelAnimation coppiedAnimation = new DefaultSceneModelAnimation(a.getValue());
			m_animations.put(a.getKey(), coppiedAnimation);
			
			if(source.m_currentAnimation == a.getValue())
				m_currentAnimation = coppiedAnimation;
		}
		
		m_direction = source.m_direction;
	}
	
	@Override
	public ISceneModel clone()
	{
		return new DefaultSceneModel(this);
	}
	
	void addAnimation(String name, DefaultSceneModelAnimation animation)
	{
		m_animations.put(name, animation);
		refreshCurrentAnimation();
	}
	
	@Override
	public List<ISceneModelComponent> getComponents()
	{
		if(m_currentAnimation == null)
			return new ArrayList<>();
			
		return m_currentAnimation.getComponents();
	}
	
	@Override
	public Rect3F getAABB()
	{
		if(m_currentAnimation == null)
			return new Rect3F();
		
		return m_currentAnimation.getAABB();
	}

	@Override
	public ISceneModelAnimation getAnimation(String name) throws NoSuchAnimationException
	{
		ISceneModelAnimation animation = m_animations.get(name);
		
		if(animation == null)
			throw new NoSuchAnimationException(name);
	
		return animation;
	}
	
	@Override
	public void addObserver(ISceneModelObserver o)
	{
		m_observers.add(o);
	}
	
	@Override
	public void removeObserver(ISceneModelObserver o)
	{
		m_observers.remove(o);
	}

	private void refreshCurrentAnimation()
	{
		if(m_currentAnimation != null && m_currentAnimation.getState() == SceneModelAnimationState.Stop)
			m_currentAnimation = null;
		
		for(DefaultSceneModelAnimation a : m_animations.values())
		{
			if(a.m_state != SceneModelAnimationState.Stop)
			{
				m_currentAnimation = a;
				break;
			}
		}
	}
	
	@Override
	public void update(int delta)
	{
		if(m_currentAnimation != null)
			m_currentAnimation.update(delta);
	}
	
	@Override
	public void setDirection(Direction direction)
	{
		m_direction = direction;
	}

	@Override
	public Direction getDirection()
	{
		return m_direction;
	}
	
	public final class DefaultSceneModelAnimation implements ISceneModelAnimation
	{
		private final Map<Direction, List<DefaultSceneModelComponent>> m_components = new HashMap<>();
		private final Map<Direction, Rect3F> m_aabbs = new HashMap<>();
		
		private SceneModelAnimationState m_state = SceneModelAnimationState.Stop;
		
		DefaultSceneModelAnimation() { }
		
		DefaultSceneModelAnimation(DefaultSceneModelAnimation source)
		{
			m_state = source.m_state;
			
			for(Map.Entry<Direction, List<DefaultSceneModelComponent>> e : source.m_components.entrySet())
			{
				ArrayList<DefaultSceneModelComponent> coppiedComponents = new ArrayList<>();
				
				for(DefaultSceneModelComponent c : e.getValue())
					coppiedComponents.add(new DefaultSceneModelComponent(c));
				
				m_aabbs.put(e.getKey(), source.m_aabbs.get(e.getKey()));
				m_components.put(e.getKey(), coppiedComponents);
			}
		}
		
		private void mergeAABB(Direction direction, Rect3F aabb)
		{
			if(!m_aabbs.containsKey(direction))
				m_aabbs.put(direction, new Rect3F());
			
			Rect3F destAabb = m_aabbs.get(direction);
			float maxX = Math.max(destAabb.x + destAabb.width, aabb.x + aabb.width);
			float maxY = Math.max(destAabb.y + destAabb.height, aabb.y + aabb.height);
			float maxZ = Math.max(destAabb.z + destAabb.depth, aabb.z + aabb.depth);
			
			destAabb.x = Math.min(aabb.x, destAabb.x);
			destAabb.y = Math.min(aabb.y, destAabb.y);
			destAabb.z = Math.min(aabb.z, destAabb.z);
			destAabb.width = maxX - destAabb.x;
			destAabb.height = maxY - destAabb.y;
			destAabb.depth = maxZ - destAabb.z;
		}

		Rect3F getAABB()
		{
			Rect3F aabb = m_aabbs.get(m_direction);
			
			return aabb == null ? new Rect3F() : new Rect3F(aabb);
		}
		
		@Override
		public void setState(SceneModelAnimationState state)
		{
			m_state = state;
			
			for(List<DefaultSceneModelComponent> l : m_components.values())
			{
				for(DefaultSceneModelComponent c : l)
					c.setState(state);
			}
			
			refreshCurrentAnimation();
		}
		
		private List<ISceneModelComponent> getComponents()
		{
			List<DefaultSceneModelComponent> components = m_components.get(m_direction);
		
			return components == null ? new ArrayList<ISceneModelComponent>() : new ArrayList<ISceneModelComponent>(components);
		}
		
		void addComponent(Direction direction, DefaultSceneModelComponent component)
		{
			List<DefaultSceneModelComponent> components = m_components.get(direction);
			
			if(components == null)
			{
				components = new ArrayList<DefaultSceneModelComponent>();
				m_components.put(direction, components);
			}
			
			components.add(component);
			mergeAABB(direction, component.getBounds());
			
			component.addObserver(new IDefaultSceneModelComponentObserver() {
				@Override
				public void onFrameEvent(String name) {
					m_observers.onEvent(name);
				}
			});
		}

		@Override
		public SceneModelAnimationState getState()
		{
			if(m_currentAnimation != this)
				return SceneModelAnimationState.Stop;
			else
				return m_state;
		}
		
		private void update(int delta)
		{
			List<DefaultSceneModelComponent> components = m_components.get(m_direction);
			
			if(components != null)
			{
				for(DefaultSceneModelComponent c : components)
					c.update(delta);
			}
		}
	}
	
	private static final class Observers extends StaticSet<ISceneModelObserver>
	{
		void onEvent(String name)
		{
			for(ISceneModelObserver o : this)
				o.onEvent(name);
		}
	}
}
