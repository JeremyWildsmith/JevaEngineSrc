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
package io.github.jevaengine.world;

import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.search.ISearchFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class EffectMap
{
	private HashMap<Vector2D, TileEffects> m_tileEffects;

	public EffectMap()
	{
		m_tileEffects = new HashMap<Vector2D, TileEffects>();
	}

	public EffectMap(EffectMap map)
	{
		m_tileEffects = new HashMap<Vector2D, TileEffects>();

		for (Map.Entry<Vector2D, TileEffects> effects : map.m_tileEffects.entrySet())
			applyOverlayEffects(effects.getKey(), effects.getValue());
	}

	public void clear()
	{
		m_tileEffects.clear();
	}

	public TileEffects getTileEffects(Vector2D location)
	{
		if (!m_tileEffects.containsKey(location))
			return new TileEffects();

		return m_tileEffects.get(location);
	}

	public final TileEffects[] getTileEffects(ISearchFilter<TileEffects> filter)
	{
		ArrayList<TileEffects> tileEffects = new ArrayList<TileEffects>();

		Rect2D searchBounds = filter.getSearchBounds();

		for (int x = searchBounds.x; x <= searchBounds.x + searchBounds.width; x++)
		{
			for (int y = searchBounds.y; y <= searchBounds.y + searchBounds.height; y++)
			{
				TileEffects effects = getTileEffects(new Vector2D(x, y));

				if (effects != null && filter.shouldInclude(new Vector2F(x, y)) && (effects = filter.filter(effects)) != null)
				{
					tileEffects.add(effects);
				}
			}
		}

		return tileEffects.toArray(new TileEffects[tileEffects.size()]);
	}

	public final void applyOverlayEffects(ISearchFilter<TileEffects> filter, TileEffects overlay)
	{
		Rect2D searchBounds = filter.getSearchBounds();

		for (int x = searchBounds.x; x <= searchBounds.width; x++)
		{
			for (int y = searchBounds.y; y <= searchBounds.height; y++)
			{
				TileEffects effects = getTileEffects(new Vector2D(x, y));

				if (filter.shouldInclude(new Vector2F(x, y)) && effects == null)
				{
					m_tileEffects.put(new Vector2D(x, y), overlay);
					effects = overlay;
				}

				if (effects != null && filter.shouldInclude(new Vector2F(x, y)) && (effects = filter.filter(effects)) != null)
				{
					effects.overlay(overlay);
				}
			}
		}
	}

	public final void applyOverlayEffects(Vector2D location, TileEffects value)
	{
		if (!m_tileEffects.containsKey(location))
			m_tileEffects.put(location, value);
		else
			m_tileEffects.put(location, m_tileEffects.get(location).overlay(value));
	}

	public final void overlay(EffectMap overlay, Vector2D offset)
	{
		for (Map.Entry<Vector2D, TileEffects> effects : overlay.m_tileEffects.entrySet())
			applyOverlayEffects(effects.getKey().add(offset), effects.getValue());
	}

	public final void overlay(EffectMap overlay)
	{
		overlay(overlay, new Vector2D());
	}

	public static class TileEffects
	{
		private boolean isTraversable;
		private float sightEffect;
		
		public TileEffects()
		{
			isTraversable = true;
			sightEffect = 1.0F;
		}

		public TileEffects(TileEffects effects)
		{
			isTraversable = effects.isTraversable;
			sightEffect = effects.sightEffect;
		}

		public TileEffects(boolean _isTraversable)
		{
			isTraversable = _isTraversable;
			sightEffect = 1.0F;
		}

		public TileEffects(float _sightEffect)
		{
			isTraversable = true;
			sightEffect = _sightEffect;
		}

		public boolean isTraversable()
		{
			return isTraversable;
		}
		
		public float getSightEffect()
		{
			return sightEffect;
		}
		
		public static TileEffects merge(TileEffects[] tiles)
		{
			TileEffects effect = new TileEffects();

			for (TileEffects tile : tiles)
				effect = effect.overlay(tile);

			return effect;
		}

		public TileEffects overlay(TileEffects overlay)
		{
			TileEffects newEffects = new TileEffects();
			newEffects.isTraversable = isTraversable && overlay.isTraversable;
			newEffects.sightEffect = Math.min(sightEffect, overlay.sightEffect);

			return newEffects;
		}
	}
}
