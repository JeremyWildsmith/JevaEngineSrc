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

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Rect2F;
import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.entity.DefaultEntity;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.physics.IImmutablePhysicsBody;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.IPhysicsBodyObserver;
import io.github.jevaengine.world.physics.IPhysicsWorld;
import io.github.jevaengine.world.physics.PhysicsBodyDescription;
import io.github.jevaengine.world.physics.PhysicsBodyDescription.PhysicsBodyShape;
import io.github.jevaengine.world.physics.PhysicsBodyDescription.PhysicsBodyType;
import io.github.jevaengine.world.scene.ISceneBuffer;
import io.github.jevaengine.world.search.ISearchFilter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SceneGraph implements IDisposable
{
	private final ArrayList<EntitySector> m_sectors = new ArrayList<>();
	private final ArrayList<EntityEntry> m_entities = new ArrayList<>();
	private final ArrayList<EntityEntry> m_dynamicEntities = new ArrayList<>();
	
	private final Queue<EntityEntry> m_entityUpdateQueue = new LinkedList<>();
	
	private final Observers m_observers = new Observers();
	
	private final IPhysicsWorld m_hostWorld;
	
	public SceneGraph(IPhysicsWorld hostWorld)
	{
		m_hostWorld = hostWorld;
	}
	
	public void dispose()
	{
		for (EntityEntry e : m_entities)
			e.dispose();
		
		for(EntitySector s : m_sectors)
			s.dispose();
		
		m_sectors.clear();
		m_entities.clear();
	}

	public void addObserver(EntityContainerObserver o)
	{
		m_observers.add(o);
	}
	
	public void removeObserver(EntityContainerObserver o)
	{
		m_observers.remove(o);
	}
	
	private EntitySector getSector(Vector2F location)
	{
		LayerSectorCoordinate sectorCoordinates = new LayerSectorCoordinate(location);
		
		if (!m_sectors.contains(sectorCoordinates))
			m_sectors.add(new EntitySector(location));

		int sec = m_sectors.indexOf(sectorCoordinates);

		EntitySector sector = m_sectors.get(sec);
		
		return sector;
	}
	
	@Nullable
	private EntityEntry getEntityEntry(IEntity entity)
	{
		for(EntityEntry entry : m_entities)
		{
			if(entry.getSubject().equals(entity))
				return entry;
		}
		
		return null;
	}
	
	public EntitySet getEntities(@Nullable Rect2D region)
	{
		return new EntitySet(region);
	}
	
	public EntitySet getEntities()
	{
		return getEntities(null);
	}
	
	public void add(IEntity entity)
	{
		EntityEntry entry = new EntityEntry(entity);
		
		m_entities.add(entry);
		
		if(!entity.isStatic())
		{
			//If the entity is dynamic and added during an update cycle, we must add it to the update queue.
			m_entityUpdateQueue.add(entry);
			m_dynamicEntities.add(entry);
		}
		
		m_observers.addedEntity(entity);
	}
	
	public void remove(IEntity entity)
	{
		EntityEntry entry = getEntityEntry(entity);
		
		if(entry != null)
		{
			m_entities.remove(entry);
			
			m_dynamicEntities.remove(entry);
			
			entry.dispose();
			m_observers.removedEntity(entity);
			
			//If this entity was removed in the middle of an update cycle, it must be removed from the update queue
			//as to not have it's logic update while it is not associate to our world.
			m_entityUpdateQueue.remove(entry);
		}
	}

	public TileEffects getTileEffects(Vector2D location)
	{
		int index = m_sectors.indexOf(new LayerSectorCoordinate(location));

		if (index < 0)
			return new TileEffects();

		EntitySector sector = m_sectors.get(m_sectors.indexOf(new LayerSectorCoordinate(location)));

		return sector.getTileEffects(location);
	}

	public void update(int delta)
	{
		for (EntitySector sector : m_sectors)
			sector.update(delta);
		
		m_entityUpdateQueue.clear();
		m_entityUpdateQueue.addAll(m_dynamicEntities);
		
		for(EntityEntry e; (e = m_entityUpdateQueue.poll()) != null;)
			e.getSubject().update(delta);
	}

	void enqueueRender(ISceneBuffer targetScene, Rect2F renderBounds)
	{
		HashSet<Integer> renderSectors = new HashSet<Integer>();

		int sectorX = (int)Math.floor((float)renderBounds.x / EntitySector.SECTOR_DIMENSIONS);
		int sectorY = (int)Math.floor((float)renderBounds.y / EntitySector.SECTOR_DIMENSIONS);
		int sectorWidth = (int)Math.ceil((float)renderBounds.width / (float)EntitySector.SECTOR_DIMENSIONS);
		int sectorHeight = (int)Math.ceil((float)renderBounds.height / (float)EntitySector.SECTOR_DIMENSIONS);

		for (int y = sectorY; y <= sectorY + sectorHeight; y++)
		{
			for (int x = sectorX; x <= sectorX + sectorWidth; x++)
			{
				renderSectors.add(m_sectors.indexOf(new LayerSectorCoordinate(x, y, true)));
			}
		}

		HashSet<IEntity> renderEntities = new HashSet<IEntity>();
		for (Integer i : renderSectors)
		{
			if (i >= 0)
				m_sectors.get(i).enqueueRender(renderEntities, renderBounds);
		}
		
		for(IEntity e : renderEntities)
			targetScene.addModel(e.getModel(), e, e.getBody().getLocation());
	}
	
	public interface EntityContainerObserver
	{
		void addedEntity(IEntity e);
		void removedEntity(IEntity e);
	}
	
	private static final class Observers extends StaticSet<EntityContainerObserver>
	{
		public void addedEntity(IEntity e)
		{
			for(EntityContainerObserver o : this)
				o.addedEntity(e);
		}
		
		public void removedEntity(IEntity e)
		{
			for(EntityContainerObserver o : this)
				o.removedEntity(e);
		}
	}
	
	private class EntityEntry implements IDisposable
	{
		private final IEntity m_subject;
		private final LocationObserver m_observer = new LocationObserver();
		
		private final ArrayList<EntitySector> m_containingSectors = new ArrayList<>();
		
		public EntityEntry(IEntity subject)
		{
			m_subject = subject;
			
			subject.getBody().addObserver(m_observer);
			place();
		}
		
		@Override
		public void dispose()
		{
			remove();
			m_subject.getBody().removeObserver(m_observer);
		}
		
		private IEntity getSubject()
		{
			return m_subject;
		}
		
		private void place()
		{
			Rect3F aabb = m_subject.getModel().getAABB().add(m_subject.getBody().getLocation());
			Vector2D min = aabb.min().getXy().floor();
			Vector2D max = aabb.max().getXy().ceil();
			
			for(int x = min.x; x <= max.x; x++)
			{
				for(int y = min.y; y <= max.y; y++)
				{
					EntitySector s = getSector(new Vector2F(x, y));
					s.addEntity(m_subject);
					m_containingSectors.add(s);
				}
			}
		}
		
		private void remove()
		{
			for(EntitySector s : m_containingSectors)
				s.removeEntity(m_subject);
			
			m_containingSectors.clear();
		}
		
		public void refresh()
		{
			remove();
			place();
		}
		
		private class LocationObserver implements IPhysicsBodyObserver
		{
			@Override
			public void locationSet()
			{
				refresh();
			}
			
			@Override
			public void directionSet() { }

			@Override
			public void onBeginContact(IImmutablePhysicsBody other) { }

			@Override
			public void onEndContact(IImmutablePhysicsBody other) { }
		}
	}
	
	private static class LayerSectorCoordinate
	{
		int x;
		int y;
		boolean isSectorScale;

		public LayerSectorCoordinate(int _x, int _y, boolean _isSectorScale)
		{
			x = _x;
			y = _y;
			isSectorScale = _isSectorScale;
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

				return coord.x == x && coord.y == y && coord.isSectorScale == isSectorScale;
			}
			else if (o instanceof EntitySector)
			{
				EntitySector sector = (EntitySector) o;
				
				if(isSectorScale)
					return sector.m_location.equals(new Vector2D(x, y));
				else
					return new Rect2D(sector.m_location.x * EntitySector.SECTOR_DIMENSIONS,
							sector.m_location.y * EntitySector.SECTOR_DIMENSIONS,
							EntitySector.SECTOR_DIMENSIONS,
							EntitySector.SECTOR_DIMENSIONS)
							.contains(new Vector2D(x, y));
			} else
				return false;
		}
	}

	private final class EntitySector implements IDisposable
	{
		protected static final int SECTOR_DIMENSIONS = 10;

		private final ArrayList<IEntity> m_dynamic =  new ArrayList<>();
		private final ArrayList<IEntity> m_static = new ArrayList<>();

		private final EffectMap m_staticEffectMap = new EffectMap();
		private final EffectMap m_dynamicEffectMap = new EffectMap();

		private final Vector2D m_location;
		private boolean m_isDirty = false;
		
		private final IPhysicsBody m_regionSensorBody;

		public EntitySector(Vector2F containingLocation)
		{
			m_location = containingLocation.divide(SECTOR_DIMENSIONS).floor();
			m_regionSensorBody = m_hostWorld.createBody(new PhysicsBodyDescription(PhysicsBodyType.Static, PhysicsBodyShape.Box, new Rect3F(SECTOR_DIMENSIONS, SECTOR_DIMENSIONS, SECTOR_DIMENSIONS), 1.0F, true, true, 0.0F));
			m_regionSensorBody.setLocation(new Vector3F(m_location.multiply(SECTOR_DIMENSIONS).add(new Vector2D(SECTOR_DIMENSIONS / 2, SECTOR_DIMENSIONS / 2)), 0));
			m_regionSensorBody.addObserver(new RegionSensorObserver());
		}

		@Override
		public void dispose()
		{
			m_regionSensorBody.destory();
		}
		
		public void addEntity(IEntity entity)
		{
			if(m_dynamic.contains(entity) || m_static.contains(entity))
				return;
			
			if(entity.isStatic())
			{
				m_static.add(entity);
				m_isDirty = true;
			} else
				m_dynamic.add(entity);
		}
		
		public void removeEntity(IEntity entity)
		{
			m_dynamic.remove(entity);
			
			if(m_static.contains(entity))
			{
				m_static.remove(entity);
				m_isDirty = true;
			}
		}

		public List<IEntity> getEntities()
		{
			ArrayList<IEntity> all = new ArrayList<IEntity>(m_dynamic);
			all.addAll(m_static);
			
			return all;
		}
		
		public TileEffects getTileEffects(Vector2D location)
		{
			return m_staticEffectMap.getTileEffects(location).overlay(m_dynamicEffectMap.getTileEffects(location));
		}

		private void blendEffectMap(EffectMap map, IPhysicsBody body)
		{
			Rect3F bounds = body.getAABB();
			
			if(body.isCollidable() && bounds.hasVolume())
			{
				int x = (int)Math.floor(bounds.x);
				int y = (int)Math.floor(bounds.y);
				int width = (int)Math.ceil(bounds.width);
				int height = (int)Math.floor(bounds.height);
					
				for(; x < width; x++)
				{
					for(; y < height; y++)
						map.applyOverlayEffects(new Vector2D(x, y), new TileEffects(false));
				}
			}
		}
		
		public void update(int deltaTime)
		{
			m_dynamicEffectMap.clear();

			for (IEntity e : m_dynamic)
				blendEffectMap(m_dynamicEffectMap, e.getBody());
			
			if (m_isDirty)
			{
				m_staticEffectMap.clear();

				for (IEntity e : m_static)
					blendEffectMap(m_staticEffectMap, e.getBody());
				
				m_isDirty = false;
			}
		}

		public void enqueueRender(HashSet<IEntity> renderList, Rect2F renderBounds)
		{
			for (IEntity e : m_static)
			{
				Vector2F location = e.getBody().getLocation().getXy();
				
				if (renderBounds.intersects(e.getModel().getAABB().getXy().add(location)))
					renderList.add(e);
			}
			
			for (IEntity e : m_dynamic)
			{
				Vector2D location = e.getBody().getLocation().getXy().round();

				if (renderBounds.intersects(e.getModel().getAABB().getXy().add(location)))
					renderList.add(e);
			}
		}

		@Override
		public boolean equals(Object o)
		{
			if (o == this)
				return true;
			else if (o == null)
				return false;
			else if (o instanceof Vector2D || o instanceof Vector2F)
			{
				Vector2D src = (o instanceof Vector2F) ? ((Vector2F) o).floor() : ((Vector2D) o);
				return new Rect2D(m_location.x * SECTOR_DIMENSIONS, m_location.y * SECTOR_DIMENSIONS, SECTOR_DIMENSIONS, SECTOR_DIMENSIONS).contains(src);
			} else
				return false;
		}
		
		public class RegionSensorObserver implements IPhysicsBodyObserver
		{

			@Override
			public void locationSet() { }

			@Override
			public void directionSet() { }

			@Override
			public void onBeginContact(IImmutablePhysicsBody other)
			{
				if(!other.hasOwner())
					return;
				
				EntityEntry e = getEntityEntry(other.getOwner());
				
				if(e != null)
					e.refresh();
			}

			@Override
			public void onEndContact(IImmutablePhysicsBody other)
			{
				if(!other.hasOwner())
					return;
				
				EntityEntry e = getEntityEntry(other.getOwner());
				
				if(e != null)
					e.refresh();
			}
		}
	}
	
	public final class EntitySet
	{
		@Nullable
		private Rect2D m_region;
		
		private EntitySet(Rect2D region)
		{
			m_region = region;
		}
		
		private EntitySet()
		{
			this(null);
		}
		
		@Nullable
		public IEntity getByName(String name)
		{
			if(m_region != null)
			{
				for(IEntity e : getContainedEntities(m_region))
				{
					if(e.getInstanceName().equals(name))
						return e;
				}
			} else
			{
				for(EntityEntry entry : m_entities)
				{
					IEntity e = entry.getSubject();
					
					if(e.getInstanceName().equals(name))
						return e;
				}
			}
			
			return null;
		}
		
		private IEntity[] getContainedEntities(Rect2D region)
		{
			//Used to prevent entry duplication for Entities that are contained by multiple sectors.
			HashSet<IEntity> entities = new HashSet<IEntity>();
			
			for(int x = region.x; x <= region.x + region.width; x+= EntitySector.SECTOR_DIMENSIONS)
			{
				  for(int y = region.y; y < region.y + region.height; y+=EntitySector.SECTOR_DIMENSIONS)
				  {
					  entities.addAll(getSector(new Vector2F(x, y)).getEntities());
				  }
			}
			
			return entities.toArray(new IEntity[entities.size()]);
		}
		
		public IEntity[] search(ISearchFilter<DefaultEntity> filter)
		{
			//if m_region == null, use filter's search bounds,
			//otherwise use intersecting area.
			
			Rect2D searchBounds = m_region == null ? filter.getSearchBounds() : m_region.getOverlapping(filter.getSearchBounds());
			
			ArrayList<IEntity> found = new ArrayList<IEntity>();

			for (IEntity entity : getContainedEntities(searchBounds))
			{
				if (filter.shouldInclude(entity.getBody().getLocation().getXy()))
					found.add(entity);
			}

			return found.toArray(new IEntity[found.size()]);
		}
		
		public IEntity[] all()
		{
			if(m_region != null)
				return getContainedEntities(m_region);
			
			ArrayList<IEntity> entities = new ArrayList<IEntity>();
			
			for(EntityEntry e : m_entities)
				entities.add(e.getSubject());
			
			return entities.toArray(new IEntity[m_entities.size()]);
		}
	}
}
