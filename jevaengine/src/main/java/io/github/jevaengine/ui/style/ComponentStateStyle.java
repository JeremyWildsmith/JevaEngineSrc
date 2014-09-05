package io.github.jevaengine.ui.style;

import io.github.jevaengine.IDisposable;
import io.github.jevaengine.audio.IAudioClip;
import io.github.jevaengine.graphics.IFont;
import io.github.jevaengine.graphics.IImmutableGraphic;

public final class ComponentStateStyle implements IDisposable
{
	private IFont m_font;
	private IFrameFactory m_frameFactory;
	private IAudioClip m_enterAudio;
	
	public ComponentStateStyle(IFont font, IFrameFactory frameFactory, IAudioClip enterAudio)
	{
		m_font = font;
		m_frameFactory = frameFactory;
		m_enterAudio = enterAudio;
	}
	
	@Override
	public void dispose()
	{
		m_enterAudio.dispose();
	}
	
	public IFont getFont()
	{
		return m_font;
	}
	
	public IImmutableGraphic createFrame(int desiredWidth, int desiredHeight)
	{
		return m_frameFactory.create(desiredWidth, desiredHeight);
	}
	
	public void playEnter()
	{
		m_enterAudio.play();
	}
}