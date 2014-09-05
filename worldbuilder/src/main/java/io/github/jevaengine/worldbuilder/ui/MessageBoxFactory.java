package io.github.jevaengine.worldbuilder.ui;

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
import io.github.jevaengine.util.StaticSet;

public final class MessageBoxFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/messagebox.jwl";

	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	public MessageBoxFactory(WindowManager windowManager, IWindowFactory windowFactory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
	}
	
	public MessageBox create(String message) throws WindowConstructionException
	{
		Observers observers = new Observers();
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new MessageBoxBehaviourInjector(observers, message));
		m_windowManager.addWindow(window);
		
		window.center();
		return new MessageBox(observers, window);
	}
	
	public static final class MessageBox implements IDisposable
	{
		private final Observers m_observers;
		private final Window m_window;
		
		private MessageBox(Observers observers, Window window)
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
		
		public void addObserver(IMessageBoxObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeObserver(IMessageBoxObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	public interface IMessageBoxObserver
	{
		void okay();
	}
	
	private class MessageBoxBehaviourInjector extends WindowBehaviourInjector
	{
		private final Observers m_observers;
		private final String m_message;
		
		public MessageBoxBehaviourInjector(Observers observers, String message)
		{
			m_observers = observers;
			m_message = message;
		}
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			getControl(Button.class, "btnOkay").addObserver(new IButtonObserver() {		
				@Override
				public void onPress() {
					m_observers.okay();
				}
			});
			
			getControl(TextArea.class, "txtMessage").setText(m_message);
		}
	}
	
	private static final class Observers extends StaticSet<IMessageBoxObserver>
	{
		public void okay()
		{
			for(IMessageBoxObserver o : this)
				o.okay();
		}
	}
}
