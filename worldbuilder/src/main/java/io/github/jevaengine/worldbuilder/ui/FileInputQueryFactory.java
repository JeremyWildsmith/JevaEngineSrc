package io.github.jevaengine.worldbuilder.ui;

import io.github.jevaengine.AssetConstructionException;
import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.IMessageBoxObserver;
import io.github.jevaengine.worldbuilder.ui.MessageBoxFactory.MessageBox;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FileInputQueryFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/fileInput.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	private final String m_base;
	
	public FileInputQueryFactory(WindowManager windowManager, IWindowFactory windowFactory, @Nullable String base)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
		m_base = base;
	}
	
	public FileInputQueryFactory(WindowManager windowManager, IWindowFactory windowFactory)
	{
		this(windowManager, windowFactory, null);
	}
	
	public FileInputQuery create(FileInputQueryMode mode, String query, String defaultValue) throws WindowConstructionException
	{
		Observers observers = new Observers();

		Window window = m_windowFactory.create(WINDOW_LAYOUT, new FileInputQueryBehaviourInjector(observers, query, defaultValue, mode));
		m_windowManager.addWindow(window);
			
		window.center();
		return new FileInputQuery(observers, window);
	}
	
	public static class FileInputQuery implements IDisposable
	{
		private final Observers m_observers;
		
		private final Window m_window;
		
		private FileInputQuery(Observers observers, Window window)
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
		
		public void addObserver(IFileInputQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeOberver(IFileInputQueryObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	private class FileInputQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Logger m_logger = LoggerFactory.getLogger(FileInputQueryFactory.class);
		private final Observers m_observers;
		private final String m_query;
		
		private final String m_defaultValue;
		private final FileInputQueryMode m_mode;

		public FileInputQueryBehaviourInjector(Observers observers, String query, String defaultValue, FileInputQueryMode mode)
		{
			m_observers = observers;
			m_query = query;
			m_defaultValue = defaultValue;
			m_mode = mode;
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
				
			} catch (AssetConstructionException e) {
				m_logger.error("Unable to notify use of validation failures", e);
			}
		}
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final TextArea txtValue = getControl(TextArea.class, "txtValue");
			
			getControl(Label.class, "lblQuery").setText(m_query);
			txtValue.setText(m_defaultValue);
			
			getControl(Button.class, "btnOkay").addObserver(new IButtonObserver() {
				@Override
			public void onPress() {
				URI path = new File(txtValue.getText()).toURI();
				
				if(m_base != null)
				{
					path = new File(m_base).toURI().relativize(new File(path).toURI());
					
					if(path.isAbsolute())
						displayMessage("Cannot relativize the specified path. Assure it is a child of this project's base directory.");
					else
						m_observers.okay(path.getPath());
				} else
					m_observers.okay(path.getPath());
				}
			});
			
			getControl(Button.class, "btnCancel").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_observers.cancel();
				}
			});
			
			getControl(Button.class, "btnBrowse").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					
					try
					{
						SwingUtilities.invokeAndWait(new Runnable() {
	
							@Override
							public void run()
							{
								JFileChooser c = new JFileChooser(new File(m_defaultValue));
								
								int result = 0;
								
								switch(m_mode)
								{
								case SaveFile:
									result = c.showSaveDialog(null);
									break;
								case OpenDirectory:
									c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
									result = c.showOpenDialog(null);
								break;
								case OpenFile:
									result = c.showOpenDialog(null);
									break;
								default:
									throw new RuntimeException("Unrecognized mode");
								}
								
								if(result == JFileChooser.APPROVE_OPTION)
									txtValue.setText(c.getSelectedFile().toString());
							}
						});
					} catch (InvocationTargetException | InterruptedException e)
					{
						throw new RuntimeException(e);
					}
				}
			});
		}
	}
	
	public enum FileInputQueryMode
	{
		OpenDirectory,
		OpenFile,
		SaveFile,
	}
	
	public interface IFileInputQueryObserver
	{
		void okay(String input);
		void cancel();
	}
	
	private static class Observers extends StaticSet<IFileInputQueryObserver>
	{
		public void okay(String input)
		{
			for(IFileInputQueryObserver o : this)
				o.okay(input);
		}
		
		public void cancel()
		{
			for(IFileInputQueryObserver o : this)
				o.cancel();
		}
	}
}
