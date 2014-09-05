package io.github.jevaengine.audio;

import io.github.jevaengine.IDisposable;

public interface IAudioClip extends IDisposable
{
	void play();
	void stop();
	void repeat();
	void setVolume(float volume);
	
	void addObserver(IAudioClipObserver o);
	void removeObserver(IAudioClipObserver o);
}
