package io.github.jevaengine.world.search;

import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2F;

public class RadialSearchFilter <T> implements ISearchFilter<T>
{
	private Vector2F m_center;
	private float m_radius;
	
	public RadialSearchFilter(Vector2F center, float radius)
	{
		m_center = center;
		m_radius = radius;
	}
	
	@Override
	public Rect2D getSearchBounds()
	{
		return new Rect2D((int)Math.floor(m_center.x - m_radius),
							(int)Math.floor(m_center.y - m_radius),
							(int)Math.ceil(m_radius * 2),
							(int)Math.ceil(m_radius * 2));
	}

	@Override
	public boolean shouldInclude(Vector2F location)
	{
		return location.difference(m_center).getLength() <= m_radius;
	}

	@Override
	public T filter(T item)
	{
		return item;
	}

}
