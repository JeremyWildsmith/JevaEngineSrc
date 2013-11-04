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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;

public class EffectMap
{

	private HashMap<Vector2D, TileEffects> m_tileEffects;

	private Rectangle m_bounds;

	public EffectMap()
	{
		m_bounds = null;
		m_tileEffects = new HashMap<Vector2D, TileEffects>();
	}

	public EffectMap(Rectangle bounds)
	{
		m_bounds = bounds;
		m_tileEffects = new HashMap<Vector2D, TileEffects>();
	}

	public EffectMap(EffectMap map)
	{
		m_bounds = map.m_bounds;
		m_tileEffects = new HashMap<Vector2D, TileEffects>();

		for (Map.Entry<Vector2D, TileEffects> effects : map.m_tileEffects.entrySet())
			applyOverlayEffects(effects.getKey(), effects.getValue());
	}

	public void clear()
	{
		m_tileEffects.clear();
	}

	public final TileEffects getTileEffects(Vector2D location)
	{
		if (m_bounds != null && !m_bounds.contains(new Point(location.x, location.y)))
		{
			TileEffects effects = new TileEffects(location);
			effects.isTraversable = false;

			return effects;
		}

		if (!m_tileEffects.containsKey(location))
			return new TileEffects();

		return m_tileEffects.get(location);
	}

	public final TileEffects[] getTileEffects(ISearchFilter<TileEffects> filter)
	{
		ArrayList<TileEffects> tileEffects = new ArrayList<TileEffects>();

		Rectangle searchBounds = filter.getSearchBounds();

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
		Rectangle searchBounds = filter.getSearchBounds();

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
		if (m_bounds != null && !m_bounds.contains(new Point(location.x, location.y)))
			return;

		TileEffects effect = new TileEffects(value);

		if (!m_tileEffects.containsKey(location))
			m_tileEffects.put(location, effect);
		else
			m_tileEffects.get(location).overlay(effect);

		effect.location = location;
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

		public ArrayList<IInteractable> interactables;

		public boolean isTraversable;

		public float sightEffect;

		public Vector2D location;

		public TileEffects()
		{
			location = new Vector2D();
			isTraversable = true;
			sightEffect = 1.0F;
			interactables = new ArrayList<IInteractable>();
		}

		public TileEffects(Vector2D _location)
		{
			location = _location;
			isTraversable = true;
			sightEffect = 1.0F;
			interactables = new ArrayList<IInteractable>();
		}

		public TileEffects(TileEffects effects)
		{
			location = effects.location;
			isTraversable = effects.isTraversable;
			sightEffect = effects.sightEffect;

			interactables = new ArrayList<IInteractable>(effects.interactables);
		}

		public TileEffects(boolean _isTraversable)
		{
			location = new Vector2D();
			isTraversable = _isTraversable;
			sightEffect = 1.0F;
			interactables = new ArrayList<IInteractable>();
		}

		public TileEffects(float _sightEffect)
		{
			location = new Vector2D();
			isTraversable = true;
			sightEffect = _sightEffect;
			interactables = new ArrayList<IInteractable>();
		}

		public TileEffects(IInteractable... _interactables)
		{
			location = new Vector2D();
			isTraversable = true;
			interactables = new ArrayList<IInteractable>(Arrays.asList(_interactables));
		}

		public static TileEffects merge(TileEffects[] tiles)
		{
			TileEffects effect = new TileEffects();

			for (TileEffects tile : tiles)
				effect.overlay(tile);

			return effect;
		}

		public TileEffects overlay(TileEffects overlay)
		{
			isTraversable &= overlay.isTraversable;
			sightEffect = Math.min(sightEffect, overlay.sightEffect);

			for (IInteractable i : overlay.interactables)
			{
				if (!interactables.contains(i))
					interactables.add(i);
			}

			return this;
		}
	}
}
