package io.github.jevaengine.world.scene.isometric;

import io.github.jevaengine.math.Matrix3X3;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.scene.ISceneBuffer;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelComponent;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public final class PaintersIsometricSceneBuffer implements ISceneBuffer
{
	private final TreeMap<Vector3F, ArrayList<SceneGraphicEntry>> m_renderQueue;

	private final Matrix3X3 m_worldToScreenMatrix;
	
	private Vector2D m_translation = new Vector2D();
	
	public PaintersIsometricSceneBuffer(int tileWidth, int tileHeight)
	{
		m_renderQueue = new TreeMap<Vector3F, ArrayList<SceneGraphicEntry>>(new Comparator<Vector3F>() {
			@Override
			public int compare(Vector3F a, Vector3F b) {
				if (Math.abs(b.z - a.z) > Vector3F.TOLERANCE)
					return (a.z < b.z ? -1 : 1);
				
				float distanceDifference = a.getLengthSquared() - b.getLengthSquared();
				
				if(Math.abs(distanceDifference) > Vector3F.TOLERANCE)
				{
					//If there is a difference in x, and their signs are not equal (i.e, in different quadrants)
					if(Math.abs(b.x - a.x) > Vector3F.TOLERANCE && (a.x < 0) != (b.x < 0))
						return a.x < b.x ? -1 : 1;
					else if(Math.abs(b.y - a.y) > Vector3F.TOLERANCE && (a.y < 0) != (b.y < 0))
						return a.y < b.y ? -1 : 1;
					else if(Math.abs(b.z - a.z) > Vector3F.TOLERANCE && (a.z < 0) != (b.z < 0))
						return a.z < b.z ? -1 : 1;
					else
						return distanceDifference > 0 ? 1 : -1;
				}else if (Math.abs(b.z - a.z) > Vector3F.TOLERANCE)
					return (a.z < b.z ? -1 : 1);
				else if (Math.abs(b.x - a.x) > Vector3F.TOLERANCE)
					return (a.x < b.x ? -1 : 1);
				else if (Math.abs(b.y - a.y) > Vector3F.TOLERANCE)
					return (a.y < b.y ? -1 : 1);
				else
					return 0;
			}
		});
		
		m_worldToScreenMatrix = new Matrix3X3(tileWidth / 2.0F, -tileWidth / 2.0F, 0, tileHeight / 2.0F, tileHeight / 2.0F, -tileHeight, 0, 0, 1);
	}
	
	@Override
	public void translate(Vector2D translation)
	{
		m_translation = m_translation.add(translation);
	}
	
	public Vector2F translateScreenToWorld(Vector3F screenLocation, float scale)
	{
		return m_worldToScreenMatrix.scale(scale).inverse().dot(screenLocation.difference(new Vector3F(m_translation, 0))).getXy();
	}
	
	public Vector2D translateWorldToScreen(Vector3F location, float fScale)
	{
		Vector3D translation = m_worldToScreenMatrix.scale(fScale).dot(location).add(new Vector3F(m_translation, 0)).round();
		return new Vector2D(translation.x, translation.y);
	}
	
	public Vector2D translateWorldToScreen(Vector3F location)
	{
		return translateWorldToScreen(location, 1.0F);
	}
	
	public void addModel(IImmutableSceneModel model, @Nullable IEntity dispatcher, Vector3F location)
	{
		if (!m_renderQueue.containsKey(location))
			m_renderQueue.put(location, new ArrayList<SceneGraphicEntry>());
		
		for(ISceneModelComponent c : model.getComponents())
			m_renderQueue.get(location).add(new SceneGraphicEntry(dispatcher, c));
	}
	
	public void addModel(IImmutableSceneModel model, Vector3F location)
	{
		addModel(model, null, location);
	}

	public void reset()
	{
		m_translation = new Vector2D();
		m_renderQueue.clear();
	}

	@Override
	public void render(Graphics2D g, int offsetX, int offsetY, float scale)
	{		
		for (Map.Entry<Vector3F, ArrayList<SceneGraphicEntry>> entry : m_renderQueue.entrySet())
		{	
			Vector2D renderLocation = translateWorldToScreen(entry.getKey(), scale);

			for (SceneGraphicEntry renderable : entry.getValue())
				renderable.graphic.render(g, renderLocation.x + offsetX, renderLocation.y + offsetY, scale);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends IEntity> T pick(Class<T> clazz, int x, int y, float scale)
	{
		for (Map.Entry<Vector3F, ArrayList<SceneGraphicEntry>> entry : m_renderQueue.descendingMap().entrySet())
		{
			Vector2D renderLocation = translateWorldToScreen(entry.getKey(), scale);
			
			Vector2D relativePick = new Vector2D(x - renderLocation.x, y - renderLocation.y);
			
			for (SceneGraphicEntry renderable : entry.getValue())
			{
				IEntity dispatcher = renderable.dispatcher;
					
				if(dispatcher != null &&
					clazz.isAssignableFrom(dispatcher.getClass()) &&
					renderable.graphic.testPick(relativePick.x, relativePick.y, scale))
					return (T)dispatcher;
			}
		}
		
		return null;
	}
	
	private final class SceneGraphicEntry
	{
		private ISceneModelComponent graphic;
		
		@Nullable
		private IEntity dispatcher;
		
		public SceneGraphicEntry(IEntity _dispatcher, ISceneModelComponent _graphic)
		{
			graphic = _graphic;
			dispatcher = _dispatcher;
		}
	}
}
