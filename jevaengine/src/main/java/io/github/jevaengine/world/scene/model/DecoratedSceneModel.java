package io.github.jevaengine.world.scene.model;

import io.github.jevaengine.math.Rect3F;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DecoratedSceneModel implements IImmutableSceneModel
{
	private final IImmutableSceneModel m_model;

	private final ArrayList<ISceneModelComponent> m_additionalComponents = new ArrayList<>();
	
	public DecoratedSceneModel(IImmutableSceneModel model, ISceneModelComponent ... additionalComponents)
	{
		m_model = model;
		m_additionalComponents.addAll(Arrays.asList(additionalComponents));
	}
	
	@Override
	public ISceneModel clone()
	{
		return m_model.clone();
	}
	
	@Override
	public List<ISceneModelComponent> getComponents()
	{
		ArrayList<ISceneModelComponent> components = new ArrayList<>();
		
		components.addAll(m_model.getComponents());
		components.addAll(m_additionalComponents);
		
		return components;
	}

	@Override
	public Rect3F getAABB()
	{
		return m_model.getAABB();
	}

	@Override
	public void addObserver(ISceneModelObserver o)
	{
		m_model.addObserver(o);
	}

	@Override
	public void removeObserver(ISceneModelObserver o)
	{
		m_model.removeObserver(o);
	}

	@Override
	public void update(int delta)
	{
		m_model.update(delta);
	}
}
