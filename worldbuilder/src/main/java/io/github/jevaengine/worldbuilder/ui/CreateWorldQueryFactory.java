package io.github.jevaengine.worldbuilder.ui;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.IMessageBoxObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.MessageBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CreateWorldQueryFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/createWorld.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	public CreateWorldQueryFactory(WindowManager windowManager, IWindowFactory windowFactory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
	}
	
	public CreateWorldQuery create() throws WindowConstructionException
	{
		Observers observers = new Observers();
			
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new CreateWorldQueryBehaviourInjector(observers));
		m_windowManager.addWindow(window);
			
		window.center();
		return new CreateWorldQuery(observers, window);
		
	}
	
	public static class CreateWorldQuery implements IDisposable
	{
		private final Observers m_observers;
		
		private final Window m_window;
		
		private CreateWorldQuery(Observers observers, Window window)
		{
			m_observers = observers;
			m_window = window;
		}
		
		@Override
		public void dispose()
		{
			m_window.dispose();
		}
		
		public void setVisible(boolean isVisible)
		{
			m_window.setVisible(isVisible);
		}
		
		public void setLocation(Vector2D location)
		{
			m_window.setLocation(location);
		}
		
		public void center()
		{
			m_window.center();
		}
		
		public void addObserver(ICreateWorldQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeOberver(ICreateWorldQueryObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	private class CreateWorldQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Logger m_logger = LoggerFactory.getLogger(CreateWorldQueryBehaviourInjector.class);
		private final Observers m_observers;

		public CreateWorldQueryBehaviourInjector(Observers observers)
		{
			m_observers = observers;
		}
		
		private void validationFailed(String cause)
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
				
			} catch (AssetConstructionException e) {
				m_logger.error("Unable to notify use of validation failures", e);
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
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final TextArea txtWidth = getControl(TextArea.class, "txtWidth");
			final TextArea txtHeight = getControl(TextArea.class, "txtHeight");
			final TextArea txtTileWidth = getControl(TextArea.class, "txtTileWidth");
			final TextArea txtTileHeight = getControl(TextArea.class, "txtTileHeight");
			final TextArea txtFriction = getControl(TextArea.class, "txtFriction");
			
			getControl(Button.class, "btnOkay").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					Integer width = parseInteger(txtWidth.getText());
					Integer height = parseInteger(txtHeight.getText());
					Integer tileWidth = parseInteger(txtTileWidth.getText());
					Integer tileHeight = parseInteger(txtTileHeight.getText());
					Float friction = parseFloat(txtFriction.getText());
					
					if(width == null || width <= 0)
						validationFailed("'Width' property must be a properly formed number greater than 0.");
					else if(height == null || height <= 0)
						validationFailed("'Height' property must be a properly formed number greater than 0.");
					else if(tileWidth == null || tileWidth <= 0)
						validationFailed("'Tile Width' property must be a properly formed number greater than 0.");
					else if(tileHeight == null || tileHeight <= 0)
						validationFailed("'Tile Height' property must be a properly formed number greater than 0.");
					else if(friction == null || friction < 0)
						validationFailed("'Friction' property must be a properly formed floating point greater than 0.");
					else
						m_observers.okay(width, height, tileWidth, tileHeight, friction);
				}
			});
			
			getControl(Button.class, "btnCancel").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_observers.cancel();
				}
			});
		}
	}
	
	public interface ICreateWorldQueryObserver
	{
		void okay(int width, int height, int tileWidth, int tileHeight, float friction);
		void cancel();
	}
	
	private static class Observers extends StaticSet<ICreateWorldQueryObserver>
	{
		public void okay(int width, int height, int tileWidth, int tileHeight, float friction)
		{
			for(ICreateWorldQueryObserver o : this)
				o.okay(width, height, tileWidth, tileHeight, friction);
		}
		
		public void cancel()
		{
			for(ICreateWorldQueryObserver o : this)
				o.cancel();
		}
	}

}
