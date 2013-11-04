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
import java.util.HashSet;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.world.EffectMap.TileEffects;

public class WorldLayer implements IDisposable
{

	private ArrayList<LayerSector> m_sectors;

	public WorldLayer()
	{
		m_sectors = new ArrayList<LayerSector>();
	}

	public void addStaticTile(Tile tile)
	{
		if (!m_sectors.contains(new LayerSectorCoordinate(tile.getLocation())))
			m_sectors.add(new LayerSector(tile.getLocation().round()));

		int sec = m_sectors.indexOf(new LayerSectorCoordinate(tile.getLocation()));

		m_sectors.get(sec).addStaticTile(tile);
	}

	public void addDynamicTile(Tile tile)
	{
		if (!m_sectors.contains(tile.getLocation()))
			m_sectors.add(new LayerSector(tile.getLocation().round()));

		int sec = m_sectors.indexOf(new LayerSectorCoordinate(tile.getLocation()));

		m_sectors.get(sec).addDynamicTile(tile);
	}

	public TileEffects getTileEffects(Vector2D location)
	{
		int index = m_sectors.indexOf(new LayerSectorCoordinate(location));

		if (index < 0)
			return new TileEffects();

		LayerSector sector = m_sectors.get(m_sectors.indexOf(new LayerSectorCoordinate(location)));

		return sector.getTileEffects(location);
	}

	public void update(int delta)
	{
		for (LayerSector sector : m_sectors)
			sector.update(delta);
	}

	public void enqueueRender(Rectangle renderBounds)
	{
		HashSet<Integer> renderSectors = new HashSet<Integer>();

		int sectorX = renderBounds.x / LayerSector.SECTOR_DIMENSIONS;
		int sectorY = renderBounds.y / LayerSector.SECTOR_DIMENSIONS;
		int sectorWidth = Math.max(3, renderBounds.width / LayerSector.SECTOR_DIMENSIONS);
		int sectorHeight = Math.max(3, renderBounds.width / LayerSector.SECTOR_DIMENSIONS);

		for (int y = sectorY; y < sectorY + sectorHeight; y++)
		{
			for (int x = sectorX; x < sectorX + sectorWidth; x++)
			{
				renderSectors.add(m_sectors.indexOf(new LayerSectorCoordinate(x * LayerSector.SECTOR_DIMENSIONS, y * LayerSector.SECTOR_DIMENSIONS)));
			}
		}

		for (Integer i : renderSectors)
		{
			if (i >= 0)
				m_sectors.get(i).enqueueRender(renderBounds);
		}
	}

	private static class LayerSectorCoordinate
	{

		int x;

		int y;

		public LayerSectorCoordinate(int _x, int _y)
		{
			x = _x;
			y = _y;
		}

		public LayerSectorCoordinate(Vector2F location)
		{
			x = location.round().x;
			y = location.round().y;
		}

		public LayerSectorCoordinate(Vector2D location)
		{
			x = location.x;
			y = location.y;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			if (o == this)
				return true;
			else if (o == null)
				return false;
			else if (o instanceof LayerSectorCoordinate)
			{
				LayerSectorCoordinate coord = (LayerSectorCoordinate) o;

				return coord.x == x && coord.y == y;
			}
			else if (o instanceof LayerSector)
			{
				LayerSector sector = (LayerSector) o;

				return new Rectangle(sector.m_location.x * LayerSector.SECTOR_DIMENSIONS,
						sector.m_location.y * LayerSector.SECTOR_DIMENSIONS,
						LayerSector.SECTOR_DIMENSIONS,
						LayerSector.SECTOR_DIMENSIONS)
						.contains(x, y);
			} else
				return false;
		}
	}

	private static class LayerSector implements IDisposable
	{

		protected static final int SECTOR_DIMENSIONS = 40;

		private Vector2D m_location;

		private ArrayList<Tile> m_dynamicTiles;

		private ArrayList<Tile> m_staticTiles;

		private EffectMap m_staticEffectMap;

		private EffectMap m_dynamicEffectMap;

		private boolean m_isDirty;

		public LayerSector(Vector2D containingLocation)
		{
			m_location = new Vector2D(containingLocation.x / SECTOR_DIMENSIONS, containingLocation.y / SECTOR_DIMENSIONS);
			m_dynamicTiles = new ArrayList<Tile>();
			m_staticTiles = new ArrayList<Tile>();

			m_staticEffectMap = new EffectMap();
			m_dynamicEffectMap = new EffectMap();

			m_isDirty = false;
		}

		public void addDynamicTile(Tile tile)
		{
			m_dynamicTiles.add(tile);
			m_isDirty = true;
		}

		public void addStaticTile(Tile tile)
		{
			m_staticTiles.add(tile);
			m_isDirty = true;
		}

		public TileEffects getTileEffects(Vector2D location)
		{
			return m_staticEffectMap.getTileEffects(location).overlay(m_dynamicEffectMap.getTileEffects(location));
		}

		public void update(int deltaTime)
		{
			m_dynamicEffectMap.clear();

			for (Tile t : m_dynamicTiles)
			{
				t.blendEffectMap(m_dynamicEffectMap);
				t.update(deltaTime);
			}

			if (m_isDirty)
			{
				m_staticEffectMap.clear();

				for (Tile t : m_staticTiles)
					t.blendEffectMap(m_staticEffectMap);

				m_isDirty = false;
			}
		}

		public void enqueueRender(Rectangle renderBounds)
		{
			for (Tile t : m_staticTiles)
			{
				Vector2D location = t.getLocation().round();

				if (renderBounds.contains(new Point(location.x, location.y)))
					t.enqueueRender();
			}
			for (Tile t : m_dynamicTiles)
			{
				Vector2D location = t.getLocation().floor();

				if (renderBounds.contains(new Point(location.x, location.y)))
					t.enqueueRender();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see io.github.jeremywildsmith.jevaengine.IDisposable#dispose()
		 */
		@Override
		public void dispose()
		{
			for (Tile t : m_dynamicTiles)
				t.disassociate();

			for (Tile t : m_staticTiles)
				t.disassociate();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object o)
		{
			if (o == this)
				return true;
			else if (o == null)
				return false;
			else if (o instanceof Vector2D || o instanceof Vector2F)
			{
				int x = (o instanceof Vector2F) ? ((Vector2F) o).floor().x : ((Vector2D) o).x;
				int y = (o instanceof Vector2F) ? ((Vector2F) o).floor().y : ((Vector2D) o).y;
				return new Rectangle(m_location.x * SECTOR_DIMENSIONS, m_location.y * SECTOR_DIMENSIONS, SECTOR_DIMENSIONS, SECTOR_DIMENSIONS).contains(x, y);
			} else
				return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.IDisposable#dispose()
	 */
	public void dispose()
	{
		for (LayerSector sector : m_sectors)
			sector.dispose();
	}
}
