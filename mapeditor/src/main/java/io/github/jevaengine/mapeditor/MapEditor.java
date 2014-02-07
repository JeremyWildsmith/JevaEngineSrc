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
package io.github.jevaengine.mapeditor;

import io.github.jevaengine.Core;
import io.github.jevaengine.ResourceFormatException;
import io.github.jevaengine.ResourceIOException;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.config.IImmutableVariable;
import io.github.jevaengine.config.JsonVariable;
import io.github.jevaengine.game.ControlledCamera;
import io.github.jevaengine.game.Game;
import io.github.jevaengine.game.IGameScriptProvider;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.graphics.Sprite.SpriteDeclaration;
import io.github.jevaengine.graphics.pipeline.Graphic;
import io.github.jevaengine.joystick.InputManager.InputKeyEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.mapeditor.EditorPane.Brush;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.ui.IWindowManager;
import io.github.jevaengine.ui.UIStyle;
import io.github.jevaengine.ui.UIStyle.UIStyleDeclaration;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.ui.WorldView.IWorldViewListener;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.IInteractable;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.World.WorldConfiguration;
import io.github.jevaengine.world.World.WorldConfiguration.EntityDeclaration;
import io.github.jevaengine.world.World.WorldConfiguration.TileDeclaration;
import io.github.jevaengine.world.WorldLayer;
import io.github.jevaengine.world.WorldLayer.LayerBackground;
import io.github.jevaengine.world.WorldLayer.LayerDeclaration;
import io.github.jevaengine.world.WorldLayer.LayerDeclaration.LayerBackgroundDeclaration;

import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class MapEditor extends Game implements IEditorPaneListener
{

	private static final String SPRITE_SPECIAL = "@tile/tile.jsf";
	private static final String ANIMATION_SPECIAL_NULL = "null";
	
	private EditorPane m_pane;

	private int m_selectedLayer;
	private HashMap<Integer, LayerMetaData> m_layerMetaData = new HashMap<Integer, LayerMetaData>();
	
	private String m_baseDirectory;

	private ControlledCamera m_camera = new ControlledCamera();
	private Vector2F m_cameraMovement = new Vector2F();

	private String m_worldScript = "";

	private @Nullable World m_world;

	private WorldView m_worldView;

	private Sprite m_cursor;
	private UIStyle m_style;
	
	public MapEditor(String baseDirectory)
	{
		m_baseDirectory = baseDirectory;
	}
	
	@Override
	protected void startup()
	{
		m_cursor = Sprite.create(Core.getService(ResourceLibrary.class).openConfiguration("@style/cursor/cursor.jsf").getValue(SpriteDeclaration.class));
		m_style = UIStyle.create(Core.getService(ResourceLibrary.class).openConfiguration("@style/editor.juis").getValue(UIStyleDeclaration.class));
		
		m_cursor.setAnimation("idle", AnimationState.Play);
		
		m_pane = new EditorPane(this, m_baseDirectory);
	
		m_selectedLayer = -1;

		Vector2D resolution = getResolution();

		m_worldView = new WorldView(resolution.x, resolution.y);
		m_worldView.setRenderBackground(false);
		m_worldView.setCamera(m_camera);

		Window worldWindow = new Window(getGameStyle(), resolution.x, resolution.y);
		worldWindow.setRenderBackground(false);
		worldWindow.addControl(m_worldView);
		worldWindow.setMovable(false);

		Core.getService(IWindowManager.class).addWindow(worldWindow);

		m_worldView.addListener(new MapViewListener());
		
		initializeWorld(5, 5, 64, 32);
		m_pane.refresh();
		m_pane.setVisible(true);
	}

	public void dispose()
	{
		m_pane.dispose();
	}
	
	@Override
	public synchronized void update(int deltaTime)
	{
		if (!m_cameraMovement.isZero())
			m_camera.move(m_cameraMovement.normalize().multiply(0.3F));

		if (m_world != null)
			m_world.update(deltaTime);

		super.update(deltaTime);
	}

	@Override
	public synchronized void initializeWorld(int worldWidth, int worldHeight, int tileWidth, int tileHeight)
	{
		m_layerMetaData.clear();
		
		m_world = new World(worldWidth, worldHeight, tileWidth, tileHeight, 0);

		WorldLayer mainLayer = new WorldLayer();

		for (int y = 0; y < worldHeight; y++)
		{
			for (int x = 0; x < worldWidth; x++)
			{
				EditorTile editorTile = new EditorTile(SPRITE_SPECIAL, ANIMATION_SPECIAL_NULL, true, true, false, 1.0F);
				editorTile.setLocation(new Vector2D(x, y));
				editorTile.addToWorld(getWorld(), mainLayer);
			}
		}

		m_selectedLayer = 0;
		m_world.addLayer(mainLayer);
		m_pane.refresh();

		m_camera.attach(m_world);
	}

	@Override
	public synchronized void refreshEntity(EditorEntity entity)
	{
		if (entity != null)
			entity.refresh(getWorld());
	}

	@Override
	public void removeEntity(EditorEntity entity)
	{
		entity.remove(getWorld());
	}

	@Override
	public synchronized void setEntityLayer(int layer)
	{
		if (getWorld() != null)
			getWorld().setEntityLayer(layer);
	}

	@Override
	public synchronized void setSelectedLayerBackground(String background) throws ResourceIOException
	{
		WorldLayer layer = getWorld().getLayers()[m_selectedLayer];

		LayerMetaData last = m_layerMetaData.get(m_selectedLayer) == null ? new LayerMetaData() : m_layerMetaData.get(m_selectedLayer);
		
		if(background.isEmpty())
			layer.setBackground(new LayerBackground(null, last.getBackgroundLocation()));
		else
			layer.setBackground(new LayerBackground(Graphic.create(background), last.getBackgroundLocation()));
		
		m_layerMetaData.put(m_selectedLayer, new LayerMetaData(background, last.getBackgroundLocation()));
	}
	
	@Override
	public synchronized void setSelectedLayerBackgroundLocation(Vector2F location) throws ResourceIOException
	{
		WorldLayer layer = getWorld().getLayers()[m_selectedLayer];
		LayerMetaData last = m_layerMetaData.get(m_selectedLayer) == null ? new LayerMetaData() : m_layerMetaData.get(m_selectedLayer);
		
		if(last.getBackground().isEmpty())
			layer.setBackground(new LayerBackground(null, location));
		else
			layer.setBackground(new LayerBackground(Graphic.create(last.getBackground()), location));
	
		
		m_layerMetaData.put(m_selectedLayer, new LayerMetaData(last.getBackground(), location));
		
	}
	
	@Override
	public synchronized LayerMetaData getSelectedLayerBackground()
	{
		if(m_layerMetaData.get(m_selectedLayer) == null)
			m_layerMetaData.put(m_selectedLayer, new LayerMetaData());
		
		return m_layerMetaData.get(m_selectedLayer);
	}
	
	@Override
	public synchronized void openWorld(IImmutableVariable source)
	{
		m_layerMetaData.clear();
	
		m_pane.clearEntities();

		WorldConfiguration worldConfig = new WorldConfiguration();
		worldConfig.deserialize(source);
		
		m_world = new World(worldConfig.worldWidth, worldConfig.worldHeight, worldConfig.tileWidth, 
							worldConfig.tileHeight, worldConfig.entityLayer, worldConfig.script);
	
		for (int l = 0; l < worldConfig.layers.length; l++)
		{
			LayerDeclaration layerDeclaration = worldConfig.layers[l];
			
			WorldLayer worldLayer = new WorldLayer();
			
			if(layerDeclaration.background != null)
			{
				m_layerMetaData.put(l, new LayerMetaData(layerDeclaration.background.texture, layerDeclaration.background.location));
				worldLayer.setBackground(new LayerBackground(Graphic.create(layerDeclaration.background.texture), layerDeclaration.background.location));
			}
			
			int[] tileIndices = layerDeclaration.tileIndices;
			
			int locationOffset = 0;
			for (int i = 0; i < tileIndices.length; i++)
			{
				if (tileIndices[i] >= 0)
				{
					if (tileIndices[i] >= worldConfig.tiles.length)
						throw new ResourceFormatException("Undeclared Tile Declaration Index Used");

					EditorTile tile;
					
					TileDeclaration tileDecl = worldConfig.tiles[tileIndices[i]];

					if (tileDecl.sprite != null)
					{
						tile = new EditorTile(tileDecl.sprite,
								tileDecl.animation, tileDecl.isTraversable,
								tileDecl.isStatic,
								tileDecl.allowRenderSplitting,
								tileDecl.visibility);
					} else
					{
						tile = new EditorTile(SPRITE_SPECIAL,
								ANIMATION_SPECIAL_NULL, tileDecl.isTraversable,
								tileDecl.isStatic,
								tileDecl.allowRenderSplitting,
								tileDecl.visibility);
					}

					tile.addToWorld(m_world, worldLayer);
					tile.setLocation(new Vector2D((locationOffset + i) % m_world.getWidth(), (int) Math.floor((locationOffset + i) / m_world.getHeight())));

				} else
					locationOffset += -tileIndices[i] - 1;
			}
			
			m_world.addLayer(worldLayer);
			
			if(worldConfig.script != null)
				m_worldScript = worldConfig.script;

			m_camera.attach(m_world);
		}
		
		for (WorldConfiguration.EntityDeclaration entityConfig : worldConfig.entities)
		{
			EditorEntity entity = new EditorEntity(entityConfig.name, entityConfig.type, entityConfig.config);
			
			if(entityConfig.location != null)
				entity.setLocation(entityConfig.location);
			
			if(entityConfig.direction != null)
				entity.setDirection(entityConfig.direction);
			
			m_pane.addEntity(entity);
			entity.refresh(m_world);
		}
		
		m_pane.refresh();
		m_camera.attach(m_world);
	}

	@Override
	public synchronized void saveWorld(FileOutputStream fileOutputStream, EditorEntity[] entities)
	{
		WorldConfiguration worldConfig = new WorldConfiguration();
		
		worldConfig.worldWidth = getWorld().getWidth();
		worldConfig.worldHeight = getWorld().getHeight();
		
		worldConfig.tileWidth = getWorld().getTileWidth();
		worldConfig.tileHeight = getWorld().getTileHeight();
		
		worldConfig.script = m_worldScript.length() == 0 ? null : m_worldScript;
		worldConfig.entityLayer = m_world.getEntityLayer();

		ArrayList<EditorTile> tiles = new ArrayList<EditorTile>();
		ArrayList<ArrayList<Integer>> layers = new ArrayList<ArrayList<Integer>>();

		for (WorldLayer layer : getWorld().getLayers())
		{
			layers.add(new ArrayList<Integer>());
			
			int accumulatedEmpty = 0;
			
			for (int y = 0; y < getWorld().getHeight(); y++)
			{
				for (int x = 0; x < getWorld().getWidth(); x++)
				{
					ArrayList<IInteractable> interactables = layer.getTileEffects(new Vector2D(x, y)).interactables;

					EditorTile editorTile = null;

					for (IInteractable i : interactables)
					{
						if (i instanceof EditorTile && 
							(((EditorTile) i).getSpriteName().compareTo(SPRITE_SPECIAL) != 0 || !((EditorTile) i).isDefaultEffects()) && 
							((EditorTile) i).getSpriteName().length() > 0)
						{
							editorTile = (EditorTile) i;
							break;
						}
					}

					if (editorTile != null && !tiles.contains(editorTile))
						tiles.add(editorTile);

					if(editorTile == null)
						accumulatedEmpty++;
					else
					{
						if(accumulatedEmpty > 0)
							layers.get(layers.size() - 1).add(-accumulatedEmpty);
						
						layers.get(layers.size() - 1).add(tiles.indexOf(editorTile));
						accumulatedEmpty = 0;
					}
				}
			}
			
			if(accumulatedEmpty > 0)
				layers.get(layers.size() - 1).add(-accumulatedEmpty);
		}

		worldConfig.tiles = new TileDeclaration[tiles.size()];
		for (int i = 0; i < tiles.size(); i++)
		{
			worldConfig.tiles[i] = new TileDeclaration();
			worldConfig.tiles[i].allowRenderSplitting = tiles.get(i).enablesSplitting();

			if(!tiles.get(i).getSpriteName().equals(SPRITE_SPECIAL))
			{
				worldConfig.tiles[i].sprite = tiles.get(i).getSpriteName();
				worldConfig.tiles[i].animation = tiles.get(i).getSpriteAnimation();
			}
			
			worldConfig.tiles[i].isStatic = tiles.get(i).isStatic();
			worldConfig.tiles[i].isTraversable = tiles.get(i).isTraversable();
			worldConfig.tiles[i].visibility = tiles.get(i).getVisibilityObstruction();
		}

		worldConfig.layers = new LayerDeclaration[layers.size()];
		for (int i = 0; i < layers.size(); i++)
		{
			worldConfig.layers[i] = new LayerDeclaration();
			
			LayerMetaData metaData = m_layerMetaData.get(i);
			if(metaData != null)
			{
				if(metaData.getBackground() != null)
				{
					LayerBackgroundDeclaration bgr = new LayerBackgroundDeclaration();
					worldConfig.layers[i].background = bgr;
					
					bgr.texture = metaData.getBackground();
					bgr.location = metaData.getBackgroundLocation();
				}
			}
			
			worldConfig.layers[i].tileIndices = new int[layers.get(i).size()];
			
			for(int x = 0; x < worldConfig.layers[i].tileIndices.length; x++)
				worldConfig.layers[i].tileIndices[x] = layers.get(i).get(x);
		}

		worldConfig.entities = new EntityDeclaration[entities.length];
		for (int i = 0; i < entities.length; i++)
		{
			worldConfig.entities[i] = new EntityDeclaration();
			worldConfig.entities[i].config = entities[i].getConfig();
			worldConfig.entities[i].direction = entities[i].getDirection();
			worldConfig.entities[i].location = entities[i].getLocation();
			worldConfig.entities[i].name = entities[i].getName();
			worldConfig.entities[i].type = entities[i].getClassName();
		}

		try
		{
			new JsonVariable(worldConfig).serialize(fileOutputStream, true);
		}catch(IOException e)
		{
			JOptionPane.showMessageDialog(m_pane, "Error saving map: " + e.toString());
		}
	}

	public World getWorld()
	{
		return m_world;
	}

	@Override
	public synchronized void selectLayer(int i)
	{
		m_selectedLayer = i;
	}

	@Override
	public synchronized void deleteSelectedLayer()
	{
		if (getWorld().getLayers().length > 1)
		{
			getWorld().removeLayer(getWorld().getLayers()[m_selectedLayer]);
			m_selectedLayer--;
		}
	}

	@Override
	public synchronized void createNewLayer()
	{
		if(m_world != null)
			m_world.addLayer(new WorldLayer());
	}

	@Override
	public synchronized void setScript(String script)
	{
		m_worldScript = script;
	}
	
	@Override
	public synchronized String getScript()
	{
		return m_worldScript;
	}

	@Override
	public synchronized int getLayers()
	{
		return m_world == null ? 0 : m_world.getLayers().length;
	}

	@Override
	public void keyDown(InputKeyEvent e)
	{
		switch (e.keyCode)
		{
			case KeyEvent.VK_UP:
				m_cameraMovement.y = -1;
				break;
			case KeyEvent.VK_RIGHT:
				m_cameraMovement.x = 1;
				break;
			case KeyEvent.VK_DOWN:
				m_cameraMovement.y = 1;
				break;
			case KeyEvent.VK_LEFT:
				m_cameraMovement.x = -1;
				break;
		}
	}

	@Override
	public void keyUp(InputKeyEvent e)
	{
		switch (e.keyCode)
		{
			case KeyEvent.VK_UP:
			case KeyEvent.VK_DOWN:
				m_cameraMovement.y = 0;
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_LEFT:
				m_cameraMovement.x = 0;
				break;
		}
	}

	@Override
	public void mouseButtonStateChanged(InputMouseEvent e)
	{
	}

	@Override
	public int getEntityLayer()
	{
		return m_world == null ? 0 : m_world.getEntityLayer();
	}

	@Override
	public UIStyle getGameStyle()
	{
		return m_style;
	}

	@Override
	protected Sprite getCursor()
	{
		return m_cursor;
	}
	
	@Override
	public IGameScriptProvider getScriptBridge()
	{
		throw new UnsupportedOperationException("Map editor does not implement a script provider.");
	}

	public class LayerMetaData
	{
		@Nullable
		private String m_background;
		private Vector2F m_location;
		
		public LayerMetaData(@Nullable String background, Vector2F location)
		{
			m_background = background;
			m_location = location;
		}
		
		public LayerMetaData()
		{
			this(null, new Vector2F());
		}
		
		@Nullable
		public String getBackground()
		{
			return m_background;
		}
		
		public Vector2F getBackgroundLocation()
		{
			return m_location;
		}
	}
	
	private class MapViewListener implements IWorldViewListener
	{
		@Override
		public void worldSelection(Vector2D screenLocation, Vector2F worldLocation, MouseButton button)
		{
			if (button != MouseButton.Left)
				return;

			if (m_selectedLayer >= 0 && m_selectedLayer < getWorld().getLayers().length)
			{
				ArrayList<IInteractable> interactables = getWorld().getLayers()[m_selectedLayer].getTileEffects(worldLocation.round()).interactables;

				EditorTile selectedTile = null;

				for (IInteractable i : interactables)
				{
					if (i instanceof EditorTile)
					{
						selectedTile = (EditorTile) i;
						break;
					}
				}

				if (selectedTile == null)
				{
					selectedTile = new EditorTile(SPRITE_SPECIAL, ANIMATION_SPECIAL_NULL, true, true, false, 1.0F);
					selectedTile.setLocation(worldLocation.round());
					selectedTile.addToWorld(getWorld(), getWorld().getLayers()[m_selectedLayer]);
				}

				Brush brush = m_pane.getBrush();
				
				if(brush.isSelection)
				{
					m_pane.selectedTile(selectedTile.getSpriteName(),
										selectedTile.getSpriteAnimation(),
										selectedTile.getLocation().x,
										selectedTile.getLocation().y,
										selectedTile.isStatic(),
										selectedTile.isTraversable(),
										selectedTile.enablesSplitting(),
										selectedTile.getVisibilityObstruction());
				}else
				{
					selectedTile.setSpriteName(brush.sprite, brush.animation);
					selectedTile.setTraversable(brush.isTraversable);
					selectedTile.setStatic(brush.isStatic);
					selectedTile.setEnableSplitting(brush.allowSplitting);
					selectedTile.setVisibilityObstruction(brush.visibility);
				}
			}
		}

		@Override
		public void worldMove(Vector2D screenLocation, Vector2F worldLocation) { }
	}
}
