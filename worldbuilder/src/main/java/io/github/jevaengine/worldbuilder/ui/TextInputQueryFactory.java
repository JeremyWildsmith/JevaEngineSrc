package io.github.jevaengine.worldbuilder.ui;

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
import io.github.jevaengine.util.StaticSet;

public final class TextInputQueryFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/textInput.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	public TextInputQueryFactory(WindowManager windowManager, IWindowFactory windowFactory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
	}
	
	public TextInputQuery create(String query, String defaultValue) throws WindowConstructionException
	{
		Observers observers = new Observers();
			
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new TextInputQueryBehaviourInjector(observers, query, defaultValue));
		m_windowManager.addWindow(window);
			
		window.center();
		return new TextInputQuery(observers, window);
	}
	
	public static class TextInputQuery implements IDisposable
	{
		private final Observers m_observers;
		
		private final Window m_window;
		
		private TextInputQuery(Observers observers, Window window)
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
		
		public void addObserver(ITextInputQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeOberver(ITextInputQueryObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	private class TextInputQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Observers m_observers;
		private final String m_query;
		private final String m_defaultValue;

		public TextInputQueryBehaviourInjector(Observers observers, String query, String defaultValue)
		{
			m_observers = observers;
			m_query = query;
			m_defaultValue = defaultValue;
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
					m_observers.okay(txtValue.getText());
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
	
	public interface ITextInputQueryObserver
	{
		void okay(String input);
		void cancel();
	}
	
	private static class Observers extends StaticSet<ITextInputQueryObserver>
	{
		public void okay(String input)
		{
			for(ITextInputQueryObserver o : this)
				o.okay(input);
		}
		
		public void cancel()
		{
			for(ITextInputQueryObserver o : this)
				o.cancel();
		}
	}
}
