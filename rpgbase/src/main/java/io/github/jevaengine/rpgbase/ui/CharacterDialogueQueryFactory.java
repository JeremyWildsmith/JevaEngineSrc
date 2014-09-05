package io.github.jevaengine.rpgbase.ui;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.rpgbase.dialogue.DialogueSession.DialogueQuery;
import io.github.jevaengine.rpgbase.dialogue.IDialogueListenerSession;
import io.github.jevaengine.rpgbase.dialogue.IDialogueListenerSession.IDialogueListenerSessionObserver;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.TextArea;
import io.github.jevaengine.ui.Viewport;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.scene.ISceneBuffer;
import io.github.jevaengine.world.scene.ISceneBufferFactory;

import java.awt.Graphics2D;

public final class CharacterDialogueQueryFactory
{
	private final String m_characterDialogueLayout;
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	private final ISceneBufferFactory m_speakerViewSceneBufferFactory;
	
	public CharacterDialogueQueryFactory(WindowManager windowManager, IWindowFactory windowFactory, String characterDialogueLayout, ISceneBufferFactory speakerViewSceneBufferFactory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
		m_characterDialogueLayout = characterDialogueLayout;
		m_speakerViewSceneBufferFactory = speakerViewSceneBufferFactory;
	}
	
	public CharacterDialogueQuery create(IDialogueListenerSession session) throws WindowConstructionException
	{
		Observers observers = new Observers();
		
		Window window = m_windowFactory.create(m_characterDialogueLayout, new CharacterDialogueQueryBehaviourInjector(observers, session));
		m_windowManager.addWindow(window);

		window.center();
		
		return new CharacterDialogueQuery(observers, window);
	}
	
	public static class CharacterDialogueQuery implements IDisposable
	{
		private final Observers m_observers;
		private final Window m_window;
		
		private CharacterDialogueQuery(Observers observers, Window window)
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
		
		public void addObserver(ICharacterDialogueQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeObserver(ICharacterDialogueQueryObserver o)
		{
			m_observers.remove(o);
		}
	}

	public interface ICharacterDialogueQueryObserver
	{
		void sessionEnded();
	}
	
	private static class Observers extends StaticSet<ICharacterDialogueQueryObserver>
	{
		public void sessionEnded()
		{
			for(ICharacterDialogueQueryObserver o : this)
				o.sessionEnded();
		}
	}
	
	private class CharacterDialogueQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Observers m_observers;
		private final IDialogueListenerSession m_listenerSession;
		
		private String[] m_currentAnswers = new String[0];
		private int m_answersOffset = 0;
		
		public CharacterDialogueQueryBehaviourInjector(Observers observers, IDialogueListenerSession listenerSession)
		{
			m_observers = observers;
			m_listenerSession = listenerSession;
		}
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final TextArea txtQuery = getControl(TextArea.class, "txtQuery");
			final Viewport speakerView = getControl(Viewport.class, "speakerView");
			
			final Button[] btnAnswers = new Button[] {
					getControl(Button.class, "btnAnswer0"),
					getControl(Button.class, "btnAnswer1"),
					getControl(Button.class, "btnAnswer2"),
					getControl(Button.class, "btnAnswer3"),
					getControl(Button.class, "btnAnswer4"),
			};
			
			final Button btnMoreAnswers = getControl(Button.class, "btnMoreAnswer");
			
			btnMoreAnswers.setVisible(false);
			
			for(final Button b : btnAnswers)
			{
				b.setVisible(false);
				b.addObserver(new IButtonObserver() {
					
					@Override
					public void onPress() {
						m_listenerSession.listenerSay(b.getText());
					}
				});
			}
			
			btnMoreAnswers.addObserver(new IButtonObserver() {
				@Override
				public void onPress()
				{
					m_answersOffset = (m_answersOffset + btnAnswers.length) % m_currentAnswers.length;
					
					for(int i = 0; i < btnAnswers.length; i++)
					{
						Button answerButton = btnAnswers[i];
					
						if(m_answersOffset + i < m_currentAnswers.length)
						{
							answerButton.setText(m_currentAnswers[m_answersOffset + i]);
							answerButton.setVisible(true);
						}else
							answerButton.setVisible(false);
					}
				}
			});
			
			final ISceneBuffer speakerSceneBuffer = m_speakerViewSceneBufferFactory.create();
			
			IDialogueListenerSessionObserver uiConfigure = new IDialogueListenerSessionObserver() {

				@Override
				public void speakerInquired(final DialogueQuery query)
				{
					speakerView.setView(new IRenderable() {
						@Override
						public void render(Graphics2D g, int x, int y, float scale)
						{
							speakerSceneBuffer.reset();
							speakerSceneBuffer.addModel(query.getSpeaker().getModel(), new Vector3F());
							speakerSceneBuffer.render(g, x, y, scale);
						}
					});
					
					txtQuery.setText(query.getQuery());
					
					m_currentAnswers = query.getAnswers();
					m_answersOffset = 0;
					
					btnMoreAnswers.setVisible(m_currentAnswers.length > btnAnswers.length);
					
					for(int i = 0; i < btnAnswers.length; i++)
					{
						if(i < m_currentAnswers.length)
						{
							btnAnswers[i].setText(m_currentAnswers[i]);
							btnAnswers[i].setVisible(true);
						}else
							btnAnswers[i].setVisible(false);
					}
				}

				@Override
				public void end()
				{
					m_answersOffset = 0;
					m_currentAnswers = new String[0];
					m_observers.sessionEnded();
				}
			};
			
			m_listenerSession.addObserver(uiConfigure);
			
			DialogueQuery currentQuery = m_listenerSession.getCurrentQuery();
			
			if(currentQuery == null)
				uiConfigure.end();
			else
				uiConfigure.speakerInquired(currentQuery);
		}
	}
}