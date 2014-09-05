package io.github.jevaengine.world.scene.isometric;

import io.github.jevaengine.world.scene.ISceneBuffer;
import io.github.jevaengine.world.scene.ISceneBufferFactory;


public class PaintersIsometricSceneBufferFactory implements ISceneBufferFactory
{
	private final int m_tileWidth;
	private final int m_tileHeight;
	
	public PaintersIsometricSceneBufferFactory(int tileWidth, int tileHeight)
	{
		m_tileWidth = tileWidth;
		m_tileHeight = tileHeight;
	}
	
	@Override
	public ISceneBuffer create()
	{
		return new PaintersIsometricSceneBuffer(m_tileWidth, m_tileHeight);
	}

}
