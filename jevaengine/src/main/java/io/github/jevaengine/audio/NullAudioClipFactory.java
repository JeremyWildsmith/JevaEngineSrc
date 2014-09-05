package io.github.jevaengine.audio;

public final class NullAudioClipFactory implements IAudioClipFactory
{
	@Override
	public IAudioClip create(String name)
	{
		return new NullAudioClip();
	}
}
