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
package io.github.jevaengine.worldbuilder.world;

import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.world.DefaultWorldFactory.WorldConfiguration.SceneArtifactDeclaration;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.EffectMap.TileEffects;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.WorldAssociationException;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.NonparticipantPhysicsBody;
import io.github.jevaengine.world.physics.NullPhysicsBody;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;
import io.github.jevaengine.world.scene.model.ISceneModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class EditorSceneArtifact
{
	private static final AtomicInteger m_unnamedCount = new AtomicInteger();
	
	private String m_sceneModelName;
	private IImmutableSceneModel m_sceneModel;
	private Direction m_direction;
	private boolean m_isTraversable;

	private DummySceneArtifact m_dummy;

	public EditorSceneArtifact(IImmutableSceneModel sceneModel, String modelName, Direction direction, boolean isTraversable)
	{
		m_direction = direction;
		m_isTraversable = isTraversable;
		m_sceneModelName = modelName;
		m_sceneModel = sceneModel;
		m_dummy = new DummySceneArtifact(this.getClass().getName() + m_unnamedCount.getAndIncrement());
	}

	protected DummySceneArtifact getEntity()
	{
		return m_dummy;
	}

	public void setTraversable(boolean isTraversable)
	{
		m_isTraversable = isTraversable;
	}

	public boolean isTraversable()
	{
		return m_isTraversable;
	}
	
	public boolean isDefaultEffects()
	{
		TileEffects efx = new TileEffects();
		
		return m_isTraversable == efx.isTraversable();
	}

	public IImmutableSceneModel getModel()
	{
		return m_sceneModel.clone();
	}
	
	public void setModel(ISceneModel sceneModel, String modelName, Direction direction)
	{
		m_sceneModel = sceneModel;
		m_sceneModelName = modelName;
		m_direction = direction;
	}

	public String getModelName()
	{
		return m_sceneModelName;
	}

	/*
	 * Location shouldn't be modified unless it is through the EditorWorld
	 */
	protected void setLocation(Vector3F location)
	{
		m_dummy.getBody().setLocation(new Vector3F(location));
	}

	public Vector3F getLocation()
	{
		return new Vector3F(m_dummy.getBody().getLocation());
	}

	public Direction getDirection()
	{
		return m_direction;
	}

	public SceneArtifactDeclaration createSceneArtifactDeclaration()
	{
		SceneArtifactDeclaration artifactDecl = new SceneArtifactDeclaration();
		
		artifactDecl.isTraversable = isTraversable();
		artifactDecl.model = m_sceneModelName;
		artifactDecl.direction = m_direction;
	
		return artifactDecl;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (o == this)
			return true;
		else if (!(o instanceof EditorSceneArtifact))
			return false;

		EditorSceneArtifact tile = (EditorSceneArtifact) o;

		return (tile.m_isTraversable == this.m_isTraversable && tile.getDirection() == this.getDirection() && tile.getModelName().compareTo(this.getModelName()) == 0);
	}

	public class DummySceneArtifact implements IEntity
	{
		private final String m_name;
		private IPhysicsBody m_body = new NullPhysicsBody();
		private World m_world;
		
		public DummySceneArtifact(String name)
		{
			m_name = name;
		}
		
		public EditorSceneArtifact getEditorTile()
		{
			return EditorSceneArtifact.this;
		}
	
		@Override
		public IImmutableSceneModel getModel()
		{
			return m_sceneModel;
		}

		@Override
		public World getWorld()
		{
			return m_world;
		}

		@Override
		public void associate(World world)
		{
			if(m_world != null)
				throw new WorldAssociationException("Entity already associated to world.");
		
			m_world = world;
			m_body = new NonparticipantPhysicsBody(this);
		}

		@Override
		public void disassociate()
		{
			if(m_world == null)
				throw new WorldAssociationException("Entity already dissociated from world.");
		
			m_world = null;
			m_body = new NullPhysicsBody();
		}

		@Override
		public String getInstanceName()
		{
			return m_name;
		}

		@Override
		public Map<String, Integer> getFlags()
		{
			return new HashMap<>();
		}

		@Override
		public int getFlag(String name)
		{
			return 0;
		}

		@Override
		public boolean testFlag(String name, int value)
		{
			return false;
		}

		@Override
		public boolean isFlagSet(String name)
		{
			return false;
		}

		@Override
		public IPhysicsBody getBody()
		{
			return m_body;
		}

		@Override
		public void addObserver(IEntityObserver o) { }

		@Override
		public void removeObserver(IEntityObserver o) { }

		@Override
		public IEntityBridge getBridge()
		{
			return new PrimitiveEntityBridge(this);
		}

		@Override
		public void update(int delta) { }
		
		@Override
		public boolean isStatic() {
			return true;
		}
	}
}
