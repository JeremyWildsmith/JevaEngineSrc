package io.github.jevaengine.worldbuilder.ui;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.Checkbox;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.Direction;
import io.github.jevaengine.world.scene.model.ISceneModel;
import io.github.jevaengine.world.scene.model.ISceneModelFactory;
import io.github.jevaengine.world.scene.model.ISceneModelFactory.SceneModelConstructionException;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQuery;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQueryMode;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.IFileInputQueryObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.IMessageBoxObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.MessageBox;
import io.github.jevaengine.worldbuilder.world.ApplyTileBrushBehaviour;
import io.github.jevaengine.worldbuilder.world.Brush;
import io.github.jevaengine.worldbuilder.world.EditorSceneArtifact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SelectBrushQueryFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/selectBrush.jwl";

	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	private final ISceneModelFactory m_modelFactory;
	
	private final String m_base;
	
	public SelectBrushQueryFactory(WindowManager windowManager, IWindowFactory windowFactory, ISceneModelFactory modelFactory, String base)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
		m_modelFactory = modelFactory;
		m_base = base;
	}
	
	public SelectBrushQuery create(Brush workingBrush, @Nullable EditorSceneArtifact defaultValue) throws WindowConstructionException
	{
		Observers observers = new Observers();
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new SelectBrushQueryBehaviourInjector(observers, workingBrush, m_modelFactory, defaultValue));
		m_windowManager.addWindow(window);
			
		window.center();
		return new SelectBrushQuery(observers, window);
	}
	
	public SelectBrushQuery create(Brush workingBrush) throws WindowConstructionException
	{
		return create(workingBrush, null);
	}
	
	public static final class SelectBrushQuery implements IDisposable
	{
		private final Observers m_observers;
		private final Window m_window;
		
		private SelectBrushQuery(Observers observers, Window window)
		{
			m_observers = observers;
			m_window = window;
		}
		
		@Override
		public void dispose()
		{
			m_window.dispose();
		}
		
		public void setLocation(Vector2D location)
		{
			m_window.setLocation(location);
		}
		
		public void setVisible(boolean isVisible)
		{
			m_window.setVisible(isVisible);
		}
		
		public void addObserver(ISelectBrushQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeObserver(ISelectBrushQueryObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	public interface ISelectBrushQueryObserver
	{
		void close();
	}
	
	private class SelectBrushQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Logger m_logger = LoggerFactory.getLogger(SelectBrushQueryBehaviourInjector.class);
		
		private final EditorSceneArtifact m_defaultValue;
		private final Observers m_observers;
		private final ISceneModelFactory m_modelFactory;
		
		private Brush m_workingBrush;
		
		public SelectBrushQueryBehaviourInjector(Observers observers, Brush workingBrush, ISceneModelFactory modelFactory, EditorSceneArtifact defaultValue)
		{
			m_observers = observers;
			m_workingBrush = workingBrush;
			m_modelFactory = modelFactory;
			m_defaultValue = defaultValue;
		}
		
		private void displayMessage(String cause)
		{
			try
			{
				final MessageBox msgBox = new MessageBoxFactory(m_windowManager, m_windowFactory).create(cause);
				
				msgBox.addObserver(new IMessageBoxObserver() {
					@Override
					public void okay() {
						msgBox.dispose();
					}
				});
			} catch (AssetConstructionException e)
			{
				m_logger.error("Unable to notify user.", e);
			}
		}
		
		@Nullable
		private Float parseFloat(String s)
		{
			try
			{
				return Float.parseFloat(s);
			} catch (NumberFormatException e)
			{
				return null;
			}
		}
		
		@Nullable
		private Integer parseInteger(String s)
		{
			try
			{
				return Integer.parseInt(s);
			} catch(NumberFormatException e)
			{
				return null;
			}
		}
		
		@Nullable
		Vector2D parseVector2D(String vector)
		{
			String[] components = vector.split(",[ ]*");
			
			if(components.length != 2)
				return null;
			
			try
			{
				Vector2D buffer = new Vector2D();
				
				buffer.x = Integer.parseInt(components[0]);
				buffer.y = Integer.parseInt(components[1]);
				
				return buffer;
			}catch(NumberFormatException e)
			{
				return null;
			}
			
		}
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final TextArea txtModel = getControl(TextArea.class, "txtModel");
			final TextArea txtSize = getControl(TextArea.class, "txtSize");
			final TextArea txtDirection = getControl(TextArea.class, "txtDirection");
			
			final Checkbox chkIsTraversable = getControl(Checkbox.class, "chkIsTraversable");
			
			if(m_defaultValue != null)
			{
				txtModel.setText(m_defaultValue.getModelName());
				
				Vector2D direction = m_defaultValue.getDirection().getDirectionVector();
				txtDirection.setText(direction.x + ", " + direction.y);
				
				chkIsTraversable.setValue(m_defaultValue.isTraversable());
			}
			
			getControl(Button.class, "btnApply").addObserver(new IButtonObserver() {
				@Override
				public void onPress()
				{
					Integer size = parseInteger(txtSize.getText());
					Vector2D dirVector = parseVector2D(txtDirection.getText());
					
					if(size == null || size <= 0)
						displayMessage("Brush size must be a properly formed number greater than 0");
					else if(dirVector == null || dirVector.isZero())
						displayMessage("Direction must be a properly formed non-zero vector");
					else
					{
						try
						{
							Direction direction = Direction.fromVector(new Vector2F(dirVector));
							
							ISceneModel model = m_modelFactory.create(txtModel.getText());
							model.setDirection(direction);
							
							m_workingBrush.setSize(size);
							m_workingBrush.setBehaviour(new ApplyTileBrushBehaviour(model, txtModel.getText(), direction, chkIsTraversable.getValue()));	
						} catch (SceneModelConstructionException e)
						{
							m_logger.info("Unable to construct model for brush behaviour", e);
							displayMessage("Unable to construct model for brush behaviour. Assure you have specified a valid model resource name.");
						}
						
					}
				}
			});
			
			getControl(Button.class, "btnClose").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_observers.close();
				}
			});
			
			getControl(Button.class, "btnBrowseModel").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					try
					{
						final FileInputQuery query = new FileInputQueryFactory(m_windowManager, m_windowFactory, m_base).create(FileInputQueryMode.OpenFile, "Sprite resource", m_base);
						query.addObserver(new IFileInputQueryObserver() {
							
							@Override
							public void okay(String input) {
								txtModel.setText(input);
								query.dispose();
							}
							
							@Override
							public void cancel() {
								query.dispose();
							}
						});
					} catch (WindowConstructionException e)
					{
						m_logger.error("Unable to construct file section dialogue to select sprite resource.", e);
					}
				}
			});
		}
	}
	
	private static final class Observers extends StaticSet<ISelectBrushQueryObserver>
	{
		public void close()
		{
			for(ISelectBrushQueryObserver o : this)
				o.close();
		}
	}
}
