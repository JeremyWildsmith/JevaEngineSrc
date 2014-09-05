package io.github.jevaengine.worldbuilder.world;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.config.ValueSerializationException;
import io.github.jevaengine.config.json.JsonVariable;
import io.github.jevaengine.game.ControlledCamera;
import io.github.jevaengine.graphics.IFontFactory;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputKeyEvent.KeyEventType;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.joystick.InputMouseEvent.MouseButton;
import io.github.jevaengine.joystick.InputMouseEvent.MouseEventType;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.Timer;
import io.github.jevaengine.ui.Timer.ITimerObserver;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.Window.IWindowObserver;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.ui.WorldView;
import io.github.jevaengine.ui.WorldView.IWorldViewObserver;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.DefaultWorldFactory.WorldConfiguration;
import io.github.jevaengine.world.World;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.scene.ISceneBufferFactory;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;
import io.github.jevaengine.worldbuilder.ui.ConfigureEntityQueryFactory;
import io.github.jevaengine.worldbuilder.ui.ConfigureEntityQueryFactory.ConfigureEntityQuery;
import io.github.jevaengine.worldbuilder.ui.ConfigureEntityQueryFactory.IConfigureEntityQueryObserver;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQuery;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQueryMode;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.IFileInputQueryObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.IMessageBoxObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.MessageBox;
import io.github.jevaengine.worldbuilder.ui.SelectBrushQueryFactory;
import io.github.jevaengine.worldbuilder.ui.SelectBrushQueryFactory.ISelectBrushQueryObserver;
import io.github.jevaengine.worldbuilder.ui.SelectBrushQueryFactory.SelectBrushQuery;
import io.github.jevaengine.worldbuilder.ui.TextInputQueryFactory;
import io.github.jevaengine.worldbuilder.ui.TextInputQueryFactory.ITextInputQueryObserver;
import io.github.jevaengine.worldbuilder.ui.TextInputQueryFactory.TextInputQuery;
import io.github.jevaengine.worldbuilder.world.Brush.IBrushObserver;
import io.github.jevaengine.worldbuilder.world.SampleBrushBehaviour.IBrushSampleHandler;

import java.awt.event.KeyEvent;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EditorWorldViewFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/editorWorldView.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	private final ISceneBufferFactory m_sceneBufferFactory;
	private final ISceneModelFactory m_modelFactory;
	
	private final IFontFactory m_fontFactory;
	
	private final String m_baseDirectory;
	
	public EditorWorldViewFactory(WindowManager windowManager, IWindowFactory windowFactory,
									ISceneBufferFactory sceneBufferFactory, ISceneModelFactory modelFactory, IFontFactory fontFactory,
									String baseDirectory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
		
		m_fontFactory = fontFactory;
		m_sceneBufferFactory = sceneBufferFactory;
		m_modelFactory = modelFactory;
		m_baseDirectory = baseDirectory;
	}
	
	public EditorWorldView create(EditorWorld world) throws WindowConstructionException
	{
		Observers observers = new Observers();
		
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new EditorWorldViewBehaviourInjector(observers, world, m_sceneBufferFactory, m_modelFactory));
		m_windowManager.addWindow(window);

		return new EditorWorldView(window, observers, world.getWorld());
	}
	
	public static final class EditorWorldView implements IDisposable
	{
		private final Window m_window;
		private final Observers m_observers;
		private final World m_world;
		
		private EditorWorldView(Window window, Observers observers, World world)
		{
			m_observers = observers;
			m_window = window;
			m_world = world;
		}

		public void dispose()
		{
			m_window.dispose();
			m_world.dispose();
		}
		
		public void setVisible(boolean isVisible)
		{
			m_window.setVisible(isVisible);
		}
		
		public void setLocation(Vector2D location)
		{
			m_window.setLocation(location);
		}
		
		public void addObserver(IEditorWorldViewObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeObserver(IEditorWorldViewObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	public interface IEditorWorldViewObserver
	{
		void close();
	}
	
	private static final class Observers extends StaticSet<IEditorWorldViewObserver>
	{
		public void close()
		{
			for(IEditorWorldViewObserver o : this)
				o.close();
		}
	}
	
	private class EditorWorldViewBehaviourInjector extends WindowBehaviourInjector
	{
		private final EditorWorld m_world;
		private final ControlledCamera m_camera;
		private final ISceneModelFactory m_modelFactory;
		
		private final Observers m_observers;
		
		private final Logger m_logger = LoggerFactory.getLogger(EditorWorldViewBehaviourInjector.class);
		
		public EditorWorldViewBehaviourInjector(Observers observers, EditorWorld world, ISceneBufferFactory sceneBufferFactory, ISceneModelFactory modelFactory)
		{
			m_observers = observers;
			m_world = world;
			m_camera = new ControlledCamera(sceneBufferFactory);
			m_camera.attach(world.getWorld());
			m_modelFactory = modelFactory;
		}
		
		private void displayMessage(String message)
		{
			try
			{
				final MessageBox msgBox = new MessageBoxFactory(m_windowManager, m_windowFactory).create(message);
				msgBox.addObserver(new IMessageBoxObserver() {
					@Override
					public void okay() {
						msgBox.dispose();
					}
				});
			} catch (WindowConstructionException e)
			{
				m_logger.error("Unable to construct message dialogue", e);
			}
		}
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final Brush workingBrush = new Brush();
			
			final Timer logicTimer = new Timer();
			final WorldView worldView = getControl(WorldView.class, "worldView");
			final Label lblCursorCoordinates = getControl(Label.class, "lblCursorCoordinates");
			final Label lblIsTraversable = getControl(Label.class, "lblIsTraversable");
			
			addControl(logicTimer);			
			worldView.setCamera(m_camera);
			
			CameraController cameraController = new CameraController(worldView, m_camera, m_world);
			logicTimer.addObserver(cameraController);
			worldView.addObserver(cameraController);
			addObserver(cameraController);

			SelectionController selectionController = new SelectionController(m_world, workingBrush);
			addObserver(selectionController);
			logicTimer.addObserver(selectionController);
			
			logicTimer.addObserver(new ITimerObserver() {
				@Override
				public void update(int deltaTime) {
					m_world.getWorld().update(deltaTime);
					Vector3F coordinates = m_world.getCursor().getLocation();
					lblCursorCoordinates.setText(String.format("%f, %f, %f", coordinates.x, coordinates.y, coordinates.z));
					
					EditorSceneArtifact tile = m_world.getTile(coordinates);
					lblIsTraversable.setText(tile == null || tile.isTraversable() ? "true" : "false");
				}
			});
			
			workingBrush.addObserver(new IBrushObserver() {
				@Override
				public void behaviourChanged(IBrushBehaviour behaviour) {
					m_world.getCursor().setModel(behaviour.getModel());
				}
			});
			
			getControl(Button.class, "btnCreateEntity").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					
					final EditorEntity base = new EditorEntity(m_fontFactory, m_modelFactory, "Unnamed", "", "");
					
					try
					{
						final ConfigureEntityQuery query = new ConfigureEntityQueryFactory(m_windowManager, m_windowFactory, m_baseDirectory).create(base);
						m_world.addEntity(base);
						query.addObserver(new IConfigureEntityQueryObserver() {
							
							@Override
							public void delete() {
								m_world.removeEntity(base);
								query.dispose();
							}
							
							@Override
							public void cancel() {
								m_world.removeEntity(base);
								query.dispose();
							}
							
							@Override
							public void apply() {
								query.dispose();
							}
						});
					} catch (WindowConstructionException e)
					{
						m_logger.error("Unable to construct entity configuration dialogue.", e);
					}
					
				}
			});
			
			getControl(Button.class, "btnClose").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_observers.close();
				}
			});
			
			getControl(Button.class, "btnAdjustDepth").addObserver(new IButtonObserver() {
				@Override
				public void onPress()
				{
					try {
						final TextInputQuery query = new TextInputQueryFactory(m_windowManager, m_windowFactory).create("Cursor Depth", Float.toString(m_world.getCursor().getLocation().z));
						query.addObserver(new ITextInputQueryObserver() {
							
							@Override
							public void okay(String input)
							{
								try
								{
									Vector3F cursorLocation = m_camera.getLookAt();
									cursorLocation.z = Float.parseFloat(input);
									m_camera.lookAt(cursorLocation);;
									query.dispose();
								} catch(NumberFormatException e)
								{
									displayMessage("Depth must be a properly formed floating point.");
								}
							}
							@Override
							public void cancel() {
								query.dispose();
							}
						});
					} catch (WindowConstructionException e) {
						m_logger.error("Unable to construct text input dialogue for depth adjust", e);
					}
				}
			});
			
			getControl(Button.class, "btnApplyBrush").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					try
					{
						final SelectBrushQuery query = new SelectBrushQueryFactory(m_windowManager, m_windowFactory, m_modelFactory, m_baseDirectory).create(workingBrush);
						query.addObserver(new ISelectBrushQueryObserver() {
							@Override
							public void close() {
								query.dispose();
							}
						});
					} catch (WindowConstructionException e)
					{
						m_logger.error("Could not construt brush selection dialogue", e);
					}
				}
			});
			
			getControl(Button.class, "btnClearBrush").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					workingBrush.setBehaviour(new ClearTileBrushBehaviour());
				}
			});
			
			getControl(Button.class, "btnSampleBrush").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					workingBrush.setBehaviour(new SampleBrushBehaviour(new IBrushSampleHandler() {
						@Override
						public void sample(EditorSceneArtifact sample) {
							workingBrush.setBehaviour(new NullBrushBehaviour());
							try
							{
								final SelectBrushQuery query = new SelectBrushQueryFactory(m_windowManager, m_windowFactory, m_modelFactory, m_baseDirectory).create(workingBrush, sample);
								query.addObserver(new ISelectBrushQueryObserver() {
									@Override
									public void close() {
										query.dispose();
									}
								});
							} catch (WindowConstructionException e)
							{
								m_logger.error("Could not construt brush selection dialogue", e);
							}
						}
					}));
				}
			});
			
			getControl(Button.class, "btnSave").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					
					try {
						final FileInputQuery query = new FileInputQueryFactory(m_windowManager, m_windowFactory).create(FileInputQueryMode.SaveFile, "World Save Location", m_baseDirectory);
						query.addObserver(new IFileInputQueryObserver() {
							
							@Override
							public void okay(String input) {
								WorldConfiguration config = m_world.createWorldConfiguration();
								
								try(FileOutputStream fos = new FileOutputStream(input))
								{
									JsonVariable var = new JsonVariable();
									var.setValue(config);
									var.serialize(fos, true);
								} catch (IOException | ValueSerializationException e)
								{
									m_logger.error("Unable to save world", e);
									displayMessage("Error occured attmepting to save world. View log for more details.");
								} finally
								{
									query.dispose();
								}
							}
							
							@Override
							public void cancel() {
								query.dispose();
							}
						});
					} catch (WindowConstructionException e) {
						m_logger.error("Could not construct save world dialogue", e);
					}
				}
			});
		}
	}

	private class CameraController implements IWindowObserver, ITimerObserver, IWorldViewObserver
	{
		private final Logger m_logger = LoggerFactory.getLogger(CameraController.class);
		
		private final ControlledCamera m_camera;
		private final WorldView m_worldView;
		private final EditorWorld m_world;
		
		private Vector3F m_cameraMovement = new Vector3F();
		
		public CameraController(WorldView worldView, ControlledCamera camera, EditorWorld world)
		{
			m_worldView = worldView;
			m_camera = camera;
			m_world = world;
		}

		@Override
		public void update(int deltaTime)
		{
			if (!m_cameraMovement.isZero())
				m_camera.move(m_cameraMovement.normalize().multiply(deltaTime / 100.0F));
			
			m_world.getCursor().setLocation(new Vector3F(m_camera.getLookAt().getXy().round(), m_camera.getLookAt().z));
		}
		
		@Override
		public void onKeyEvent(InputKeyEvent e) 
		{
			if(e.type == KeyEventType.KeyDown)
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
			} else if(e.type == KeyEventType.KeyUp)
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
		}

		@Override
		public void onMouseEvent(InputMouseEvent event) { }

		@Override
		public void onFocusChanged(boolean hasFocus)
		{
			if(!hasFocus)
				m_cameraMovement = new Vector3F();
		}
		
		@Override
		public void mouseEvent(InputMouseEvent event)
		{
			if(event.mouseButton == MouseButton.Left && event.type == MouseEventType.MouseClicked)
			{
				IEntity tile = m_worldView.pick(IEntity.class, event.location);
				
				if(tile != null)
					m_camera.lookAt(tile.getBody().getLocation());
				
				if(tile instanceof EditorEntity.DummyEntity)
				{
					final EditorEntity editorEntity = ((EditorEntity.DummyEntity)tile).getEditorEntity();
					
					try
					{
						final ConfigureEntityQuery query = new ConfigureEntityQueryFactory(m_windowManager, m_windowFactory, m_baseDirectory).create(editorEntity);
					
						query.addObserver(new IConfigureEntityQueryObserver() {
							@Override
							public void delete() {
								m_world.removeEntity(editorEntity);
								query.dispose();
							}
							
							@Override
							public void cancel() {
								query.dispose();
							}
							
							@Override
							public void apply() {
								query.dispose();
							}
						});
					
					} catch (WindowConstructionException e)
					{
						m_logger.error("Unable to construct entity configuration dialogue", e);
					}
				}
			}
		}
	}
		
	private static final class SelectionController implements IWindowObserver, ITimerObserver
	{
		private final EditorWorld m_world;
		private final Brush m_brush;
				
		private boolean m_applyBrush = false;
		
		public SelectionController(EditorWorld world, Brush brush)
		{
			m_brush = brush;
			m_world = world;
		}

		@Override
		public void update(int deltaTime)
		{
			if(m_applyBrush)
				m_brush.apply(m_world);
		}

		@Override
		public void onKeyEvent(InputKeyEvent event)
		{
			if(event.keyCode != KeyEvent.VK_ENTER)
				return;
			
			if(event.type == KeyEventType.KeyUp)
				m_applyBrush = false;
			else if(event.type == KeyEventType.KeyDown)
				m_applyBrush = true;
		}

		@Override
		public void onFocusChanged(boolean hasFocus)
		{
			if(!hasFocus)
				m_applyBrush = false;
		}
		
		@Override
		public void onMouseEvent(InputMouseEvent event) { }
	}
}
