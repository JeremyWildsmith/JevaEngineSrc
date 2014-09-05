package io.github.jevaengine.audio;

import io.github.jevaengine.AssetConstructionException;

import com.google.inject.ImplementedBy;

@ImplementedBy(CachedAudioClipFactory.class)
public interface IAudioClipFactory
{
	IAudioClip create(String name) throws AudioClipConstructionException;

	public static final class AudioClipConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;
		
		public AudioClipConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
