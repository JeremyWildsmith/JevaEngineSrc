package io.github.jevaengine.worldbuilder.ui;



import io.github.jevaengine.IDisposable;
import io.github.jevaengine.graphics.AnimationState;
import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.graphics.Sprite;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.ui.Button;
import io.github.jevaengine.ui.Button.IButtonObserver;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.ui.IWindowFactory.WindowConstructionException;
import io.github.jevaengine.ui.NoSuchControlException;
import io.github.jevaengine.ui.Viewport;
import io.github.jevaengine.ui.Window;
import io.github.jevaengine.ui.WindowBehaviourInjector;
import io.github.jevaengine.ui.WindowManager;
import io.github.jevaengine.util.StaticSet;

import java.awt.Graphics2D;

public final class SelectSpriteAnimationQueryFactory
{
	private static final String WINDOW_LAYOUT = "@ui/windows/selectSpriteAnimation.jwl";
	
	private final WindowManager m_windowManager;
	private final IWindowFactory m_windowFactory;
	
	public SelectSpriteAnimationQueryFactory(WindowManager windowManager, IWindowFactory windowFactory)
	{
		m_windowManager = windowManager;
		m_windowFactory = windowFactory;
	}
	
	public SelectSpriteAnimationQuery create(Sprite sprite) throws WindowConstructionException
	{
		Observers observers = new Observers();
			
		Window window = m_windowFactory.create(WINDOW_LAYOUT, new SelectSpriteAnimationQueryBehaviourInjector(observers, sprite));
		m_windowManager.addWindow(window);
			
		window.center();
		return new SelectSpriteAnimationQuery(observers, window);
	}
	
	public static class SelectSpriteAnimationQuery implements IDisposable
	{
		private final Observers m_observers;
		
		private final Window m_window;
		
		private SelectSpriteAnimationQuery(Observers observers, Window window)
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
		
		public void addObserver(ISelectSpriteAnimationQueryObserver o)
		{
			m_observers.add(o);
		}
		
		public void removeOberver(ISelectSpriteAnimationQueryObserver o)
		{
			m_observers.remove(o);
		}
	}
	
	private class SelectSpriteAnimationQueryBehaviourInjector extends WindowBehaviourInjector
	{
		private final Observers m_observers;
		private final Sprite m_sprite;

		private final String[] m_animations;
		private int m_currentAnimation = 0;
		
		public SelectSpriteAnimationQueryBehaviourInjector(Observers observers, Sprite sprite)
		{
			m_observers = observers;
			m_sprite = sprite;
			m_animations = m_sprite.getAnimations();
		}
		
		@Override
		protected void doInject() throws NoSuchControlException
		{
			final Viewport spriteViewport = getControl(Viewport.class, "spriteViewport");
			
			spriteViewport.setView(new IRenderable() {
				@Override
				public void render(Graphics2D g, int x, int y, float scale)
				{
					m_sprite.render(g, x + spriteViewport.getBounds().width / 2, y + spriteViewport.getBounds().height / 2, scale);
				}
			});
			
			getControl(Button.class, "btnNext").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					if(m_animations.length > 0)
					{
						m_currentAnimation = (m_currentAnimation + 1) % m_animations.length;
						m_sprite.setAnimation(m_animations[m_currentAnimation], AnimationState.Play);
					}
				}
			});
			
			getControl(Button.class, "btnLast").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					if(m_animations.length > 0)
					{
						if(m_currentAnimation == 0)
							m_currentAnimation = m_animations.length - 1;
						else
							m_currentAnimation--;

						m_sprite.setAnimation(m_animations[m_currentAnimation], AnimationState.Play);
					}
				}
			});
			
			getControl(Button.class, "btnOkay").addObserver(new IButtonObserver() {
				@Override
				public void onPress() {
					m_observers.okay(m_animations.length < 0 ? "" : m_animations[m_currentAnimation]);
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
	
	public interface ISelectSpriteAnimationQueryObserver
	{
		void okay(String animation);
		void cancel();
	}
	
	private static class Observers extends StaticSet<ISelectSpriteAnimationQueryObserver>
	{
		public void okay(String animation)
		{
			for(ISelectSpriteAnimationQueryObserver o : this)
				o.okay(animation);
		}
		
		public void cancel()
		{
			for(ISelectSpriteAnimationQueryObserver o : this)
				o.cancel();
		}
	}
}
