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
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQuery;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.FileInputQueryMode;
import io.github.jevaengine.worldbuilder.ui.FileInputQueryFactory.IFileInputQueryObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.IMessageBoxObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.MessageBox;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenWorldQueryFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/openWorld.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	public OpenWorldQueryFactory(WindowManager windowManager, IWindowFactory windowFactory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
	}
	
	public OpenWorldQuery create() throws WindowConstructionException
	{
		Observers observers = new Observers();
			
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new OpenWorldQueryBehaviourInjector(observers));
		m_windowManager.addWindow(window);
			
		window.center();
		return new OpenWorldQuery(observers, window);
		
	}
	
	public static class OpenWorldQuery implements IDisposable
	{
		private final Observers m_observers;
		
		private final Window m_window;
		
		private OpenWorldQuery(Observers observers, Window window)
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
		
		public void addObserver(IOpenWorldQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeOberver(IOpenWorldQueryObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	private class OpenWorldQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Logger m_logger = LoggerFactory.getLogger(OpenWorldQueryBehaviourInjector.class);
		private final Observers m_observers;

		public OpenWorldQueryBehaviourInjector(Observers observers)
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
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final TextArea txtWorld = getControl(TextArea.class, "txtWorld");
			final TextArea txtTileWidth = getControl(TextArea.class, "txtTileWidth");
			final TextArea txtTileHeight = getControl(TextArea.class, "txtTileHeight");
			
			getControl(Button.class, "btnBrowseWorld").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					try {
						final FileInputQuery query = new FileInputQueryFactory(m_windowManager, m_windowFactory).create(FileInputQueryMode.OpenFile, "World to open", new File("").getAbsolutePath());
						query.addObserver(new IFileInputQueryObserver() {
							@Override
							public void okay(String input) {
								txtWorld.setText(input);
								query.dispose();
							}
							
							@Override
							public void cancel() {
								query.dispose();
							}
						});
					} catch (WindowConstructionException e) {
						m_logger.error("Unable to construct dialogue to browse for world", e);
					}
				}
			});
			
			getControl(Button.class, "btnOkay").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					Integer tileWidth = parseInteger(txtTileWidth.getText());
					Integer tileHeight = parseInteger(txtTileHeight.getText());
					
					if(tileWidth == null || tileWidth <= 0)
						validationFailed("'Tile Width' property must be a properly formed number greater than 0.");
					else if(tileHeight == null || tileHeight <= 0)
						validationFailed("'Tile Height' property must be a properly formed number greater than 0.");
					else
						m_observers.okay(txtWorld.getText(), tileWidth, tileHeight);
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
	
	public interface IOpenWorldQueryObserver
	{
		void okay(String world, int tileWidth, int tileHeight);
		void cancel();
	}
	
	private static class Observers extends StaticSet<IOpenWorldQueryObserver>
	{
		public void okay(String world, int tileWidth, int tileHeight)
		{
			for(IOpenWorldQueryObserver o : this)
				o.okay(world, tileWidth, tileHeight);
		}
		
		public void cancel()
		{
			for(IOpenWorldQueryObserver o : this)
				o.cancel();
		}
	}
}
