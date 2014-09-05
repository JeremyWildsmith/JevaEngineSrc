package io.github.jevaengine.audio;

public final class NullAudioClip implements IAudioClip
{
	public void dispose() { }
	
	@Override
	public void play() { }

	@Override
	public void stop() { }

	@Override
	public void repeat() { }

	@Override
	public void setVolume(float volume) { }
	

	@Override
	public void addObserver(IAudioClipObserver o) { }


	@Override
	public void removeObserver(IAudioClipObserver o) { }
}
