package io.github.jevaengine.audio;


public final class AutoDisposedAudioClip implements IAudioClip
{
	private IAudioClip m_clip;

	public AutoDisposedAudioClip(IAudioClip clip)
	{
		m_clip = clip;
		m_clip.addObserver(new IAudioClipObserver() {
			
			@Override
			public void end()
			{
				dispose();
			}
			
			@Override
			public void begin() { }
		});
	}
	
	
	@Override
	public void dispose()
	{
		if(m_clip == null)
			return;
		
		m_clip.dispose();
		m_clip = null;
	}

	@Override
	public void play()
	{
		m_clip.play();
	}

	@Override
	public void stop()
	{
		m_clip.stop();
	}

	@Override
	public void repeat()
	{
		m_clip.repeat();
	}

	@Override
	public void setVolume(float volume)
	{
		m_clip.setVolume(volume);
	}

	@Override
	public void addObserver(IAudioClipObserver o)
	{
		m_clip.addObserver(o);
	}


	@Override
	public void removeObserver(IAudioClipObserver o)
	{
		m_clip.removeObserver(o);
	}

}
