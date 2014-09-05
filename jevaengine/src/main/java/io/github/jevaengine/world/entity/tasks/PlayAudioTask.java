package io.github.jevaengine.world.entity.tasks;

import io.github.jevaengine.audio.IAudioClip;
import io.github.jevaengine.audio.IAudioClipObserver;
import io.github.jevaengine.world.entity.IEntity;

public class PlayAudioTask implements ITask
{
	private volatile boolean m_isPlaying = true;
	
	private IAudioClip m_clip;
	
	public PlayAudioTask(IAudioClip clip)
	{
		m_clip = clip;
		m_clip.stop();
		m_clip.addObserver(new IAudioClipObserver() {
			
			@Override
			public void end()
			{
				m_isPlaying = false;
			}
			
			@Override
			public void begin() { }
		});
	}
	
	@Override
	public void begin(IEntity entity)
	{
		m_clip.play();
	}

	@Override
	public void end()
	{
		m_clip.dispose();
	}

	@Override
	public void cancel()
	{
		m_clip.stop();
	}

	@Override
	public boolean doCycle(int deltaTime)
	{
		return !m_isPlaying;
	}

	@Override
	public boolean isParallel()
	{
		return true;
	}
	
}
