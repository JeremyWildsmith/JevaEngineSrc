package io.github.jevaengine.graphics;

import io.github.jevaengine.AssetConstructionException;

public interface IParticleEmitterFactory
{
	ParticleEmitter create(String name) throws ParticleEmitterConstructionException;
	
	public static final class ParticleEmitterConstructionException extends AssetConstructionException
	{
		private static final long serialVersionUID = 1L;

		public ParticleEmitterConstructionException(String assetName, Exception cause) {
			super(assetName, cause);
		}
	}
}
