package io.github.jevaengine.worldbuilder.world;

import io.github.jevaengine.config.json.JsonVariable;
import io.github.jevaengine.graphics.IFont;
import io.github.jevaengine.graphics.IFontFactory;
import io.github.jevaengine.graphics.IFontFactory.FontConstructionException;
import io.github.jevaengine.graphics.NullFont;
import io.github.jevaengine.math.Rect3F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.world.DefaultWorldFactory.WorldConfiguration.EntityImportDeclaration;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.entity.WorldAssociationException;
import io.github.jevaengine.world.physics.IPhysicsBody;
import io.github.jevaengine.world.physics.NonparticipantPhysicsBody;
import io.github.jevaengine.world.physics.NullPhysicsBody;
import io.github.jevaengine.world.scene.model.DecoratedSceneModel;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel;
import io.github.jevaengine.world.scene.model.IImmutableSceneModel.ISceneModelComponent;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;
import io.github.jevaengine.world.scene.model.ISceneModelFactory.SceneModelConstructionException;
import io.github.jevaengine.world.scene.model.NullSceneModel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EditorEntity
{
	private final Logger m_logger = LoggerFactory.getLogger(EditorEntity.class);
	
	private DummyEntity m_dummy;

	private String m_name;
	private String m_className;
	private String m_config;
	
	private IImmutableSceneModel m_sceneModel;
	
	private JsonVariable m_auxConfig = new JsonVariable();
	
	private IFont m_font;
	
	public EditorEntity(IFontFactory fontFactory, ISceneModelFactory modelFactory, String name, String className, String config)
	{
		m_font = new NullFont();
		m_sceneModel = new NullSceneModel();
		
		try
		{
			m_font = fontFactory.create("@ui/font/pro/pro.juif", Color.white);
		} catch (FontConstructionException e)
		{
			m_logger.error("Unable to construct font to render entity details. Using NullFont", e);
		}
		
		try
		{
			m_sceneModel = modelFactory.create("@entity/entity.jmf");;
		} catch (SceneModelConstructionException e)
		{
			m_logger.error("Unable to construct entity sprite. Using NullSceneGraphic border", e);
		}
		
		m_name = name;
		m_className = className;
		m_config = config;
		
		m_dummy = new DummyEntity();
	}
	
	public void setAuxiliaryConfig(JsonVariable config)
	{
		m_auxConfig = config;
	}
	
	public JsonVariable getAuxiliaryConfig()
	{
		return m_auxConfig;
	}
	
	public void setName(String name)
	{
		m_name = name;
	}

	public String getName()
	{
		return m_name;
	}

	public void setClassName(String className)
	{
		m_className = className;
	}

	public String getClassName()
	{
		return m_className;
	}

	public String getConfig()
	{
		return m_config;
	}

	public void setConfig(String config)
	{
		m_config = config;
	}
	
	public Vector3F getLocation()
	{
		return m_dummy.getBody().getLocation();
	}
	
	public void setLocation(Vector3F location)
	{
		m_dummy.getBody().setLocation(location);
	}
	
	public Direction getDirection()
	{
		return m_dummy.getBody().getDirection();
	}
	
	public void setDirection(Direction direction)
	{
		m_dummy.getBody().setDirection(direction);
	}

	protected DummyEntity getEntity()
	{
		return m_dummy;
	}
	
	public EntityImportDeclaration createImportDeclaration()
	{
		EntityImportDeclaration entityDecl = new EntityImportDeclaration();
		entityDecl.config = getConfig().trim().length() <= 0 ? null : getConfig();
		entityDecl.direction = getDirection();
		entityDecl.location = getLocation();
		entityDecl.type = getClassName();
		entityDecl.name = getName();
		entityDecl.auxConfig = m_auxConfig;
		return entityDecl;
	}

	@Override
	public String toString()
	{
		return String.format("%s of %s", m_name, m_className + (m_config.length() > 0 ? " with " + m_config : ""));
	}
	
	protected class DummyEntity implements IEntity
	{
		private IPhysicsBody m_body = new NullPhysicsBody();
		private World m_world;
		
		public DummyEntity() { }
		
		public EditorEntity getEditorEntity()
		{
			return EditorEntity.this;
		}
		
		@Override
		public IImmutableSceneModel getModel()
		{
			return new DecoratedSceneModel(m_sceneModel, new ISceneModelComponent[] {
					new ISceneModelComponent() {
						
						@Override
						public void render(Graphics2D g, int x, int y, float scale) {
							m_font.drawText(g, x, y, scale, getInstanceName());
						}
						
						@Override
						public boolean testPick(int x, int y, float scale) {
							return false;
						}
						
						@Override
						public Rect3F getBounds() {
							Vector3F mountPoint = m_sceneModel.getAABB().getPoint(0.5F, 1.1F, 0.0F);
							return new Rect3F(mountPoint, 0, 0, 0);
						}
					}
			});
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

