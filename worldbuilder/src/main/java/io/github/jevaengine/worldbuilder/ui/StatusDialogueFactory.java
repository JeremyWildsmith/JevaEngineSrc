package io.github.jevaengine.worldbuilder.ui;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.Label;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.ValueGuage;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowManager;

public class StatusDialogueFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/status.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	public StatusDialogueFactory(WindowManager windowManager, IWindowFactory windowFactory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
	}
	
	public StatusDialogue create() throws WindowConstructionException
	{

		Window window = m_windowFactory.create(WINDOW_LAYOUT);	
		m_windowManager.addWindow(window);
		window.center();
	
		try
		{
			ValueGuage statusGauge = window.getControl(ValueGuage.class, "progress");
			Label statusLabel = window.getControl(Label.class, "lblStatus");
			return new StatusDialogue(window, statusGauge, statusLabel);
		} catch (NoSuchControlException e)
		{
			window.dispose();
			throw new WindowConstructionException(WINDOW_LAYOUT, e);
		}
	}
	
	public static class StatusDialogue implements IDisposable
	{
		private final Window m_window;
		private final ValueGuage m_statusGuage;
		private final Label m_statusLabel;
		
		private StatusDialogue(Window window, ValueGuage statusGuage, Label statusLabel)
		{
			m_window = window;
			m_statusGuage = statusGuage;
			m_statusLabel = statusLabel;
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
		
		public void setStatus(String status, float progress)
		{
			m_statusGuage.setValue(progress);
			m_statusLabel.setText(status);
		}
	}
}
