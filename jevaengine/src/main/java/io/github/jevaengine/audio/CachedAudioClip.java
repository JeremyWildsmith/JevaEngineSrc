package io.github.jevaengine.audio;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineEvent.Type;
import javax.sound.sampled.LineListener;

public final class CachedAudioClip implements IAudioClip
{
	private AudioClipCache m_cache;
	
	private Clip m_clip;
	
	private float m_volume = 0.5F;

	private Map<IAudioClipObserver, AudioClipObserverAdapter> m_observers = new HashMap<>();
	
	protected CachedAudioClip(AudioClipCache cache, Clip clip)
	{
		m_cache = cache;
		m_clip = clip;
	}	

	@Override
	public void dispose()
	{
		if(m_cache == null)
			return;
		
		m_cache.freeClip(m_clip);
		m_cache = null;
		m_clip = null;
	}

	private void applyVolume()
	{
		if(m_clip == null)
			return;
		
		if(m_clip.isControlSupported(FloatControl.Type.VOLUME))
		{
			FloatControl volumeControl = (FloatControl)m_clip.getControl(FloatControl.Type.VOLUME);
				
			if(m_volume >= 0.5F)
				volumeControl.setValue(volumeControl.getMaximum() * (m_volume - 0.5F) * 2.0F);
			else
				volumeControl.setValue(volumeControl.getMinimum() * (0.5F - m_volume) * 2.0F);	
		}
	}

	@Override
	public void setVolume(float volume)
	{
		m_volume = Math.max(0, Math.min(volume, 1.0F));
	}
	
	@Override
	public void play()
	{
		if(m_clip == null)
			return;
		
		m_clip.setFramePosition(0);
		m_clip.start();
		applyVolume();
	}

	@Override
	public void stop()
	{
		if(m_clip == null)
			return;
		
		if(m_clip != null)
			m_clip.stop();
	}

	@Override
	public void repeat()
	{	
		if(m_clip == null)
			return;
		
		m_clip.setFramePosition(0);
		m_clip.loop(Clip.LOOP_CONTINUOUSLY);
		applyVolume();
	}
	
	@Override
	public void addObserver(IAudioClipObserver o)
	{
		if(m_clip == null)
			return;
		
		removeObserver(o);
		
		AudioClipObserverAdapter adapter = new AudioClipObserverAdapter(o);
		m_observers.put(o, adapter);
		m_clip.addLineListener(adapter);
	}
	
	@Override
	public void removeObserver(IAudioClipObserver o)
	{
		if(m_clip == null)
			return;
		
		AudioClipObserverAdapter adapter = m_observers.get(o);
		
		if(adapter != null)
			m_clip.removeLineListener(adapter);
	}
	
	public class AudioClipObserverAdapter implements LineListener
	{
		private final IAudioClipObserver m_observer;
		
		public AudioClipObserverAdapter(IAudioClipObserver o)
		{
			m_observer = o;
		}
		
		@Override
		public void update(LineEvent event)
		{
			if(event.getType() == Type.START)
				m_observer.begin();
			else if(event.getType() == Type.STOP)
				m_observer.end();
		}
		
	}
}
