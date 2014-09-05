package io.github.jevaengine.rpgbase.demo;

import io.github.jevaengine.IEngineThreadPool;
import io.github.jevaengine.audio.IAudioClipFactory;
import io.github.jevaengine.game.IGame;
import io.github.jevaengine.game.IGameFactory;
import io.github.jevaengine.game.IRenderer;
import io.github.jevaengine.graphics.ISpriteFactory;
import io.github.jevaengine.joystick.IInputSource;
import io.github.jevaengine.ui.IWindowFactory;
import io.github.jevaengine.world.IWorldFactory;

import javax.inject.Inject;

public final class DemoGameFactory implements IGameFactory
{
	private final IInputSource m_inputSource;
	private final IRenderer m_renderer;
	private final ISpriteFactory m_spriteFactory;
	private final IWindowFactory m_windowFactory;
	private final IWorldFactory m_worldFactory;
	private final IAudioClipFactory m_audioClipFactory;
	
	@Inject
	public DemoGameFactory(IInputSource inputSource, IRenderer renderer, ISpriteFactory spriteFactory, IWindowFactory windowFactory, IWorldFactory worldFactory, IEngineThreadPool engineThreadPool, IAudioClipFactory audioClipFactory)
	{
		m_inputSource = inputSource;
		m_renderer = renderer;
		m_spriteFactory = spriteFactory;
		m_windowFactory = windowFactory;
		m_worldFactory = worldFactory;
		m_audioClipFactory = audioClipFactory;
	}
	
	public IGame create()
	{
		return new DemoGame(m_inputSource, m_windowFactory, m_worldFactory, m_spriteFactory, m_audioClipFactory, m_renderer.getResolution());
	}
}
