package io.github.jevaengine.modelbuilder;

import io.github.jevaengine.IEngineThreadPool;
import io.github.jevaengine.game.IGame;
import io.github.jevaengine.game.IGameFactory;
import io.github.jevaengine.game.IRenderer;
import io.github.jevaengine.graphics.IFontFactory;
import io.github.jevaengine.graphics.ISpriteFactory;
import io.github.jevaengine.joystick.IInputSource;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.world.IParallelWorldFactory;
import io.github.jevaengine.world.IWorldFactory;
import io.github.jevaengine.world.ThreadPooledWorldFactory;

import javax.inject.Inject;
import javax.inject.Named;

public final class WorldBuilderFactory implements IGameFactory
{
	@Inject
	@Named("BASE_DIRECTORY")
	private String m_baseDirectory;
	
	private final IInputSource m_inputSource;
	private final IRenderer m_renderer;
	private final ISpriteFactory m_spriteFactory;
	private final IWindowFactory m_windowFactory;
	private final IParallelWorldFactory m_worldFactory;

	private final IFontFactory m_fontFactory;
	
	@Inject
	public WorldBuilderFactory(IInputSource inputSource, IRenderer renderer, ISpriteFactory spriteFactory, IWindowFactory windowFactory, IWorldFactory worldFactory, IEngineThreadPool engineThreadPool, IFontFactory fontFactory)
	{
		m_inputSource = inputSource;
		m_renderer = renderer;
		m_spriteFactory = spriteFactory;
		m_windowFactory = windowFactory;
		m_worldFactory = new ThreadPooledWorldFactory(worldFactory, engineThreadPool);
		m_fontFactory = fontFactory;
	}
	
	public IGame create()
	{
		//This field is injected typically. If this object is not instantiated by the ioc container
		//it will not be injected. The implementation assumes that it will be.
		assert m_baseDirectory != null: "BASE_DIRECTORY was not injected into WorldBuilder";
		
		return new ModelBuilder(m_inputSource, m_spriteFactory, m_windowFactory, m_worldFactory, m_fontFactory, m_renderer.getResolution(), m_baseDirectory);
	}
}
