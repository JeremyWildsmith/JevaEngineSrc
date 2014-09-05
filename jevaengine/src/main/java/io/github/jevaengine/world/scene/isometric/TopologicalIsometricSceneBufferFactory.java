package io.github.jevaengine.world.scene.isometric;

import io.github.jevaengine.world.scene.ISceneBuffer;
import io.github.jevaengine.world.scene.ISceneBufferFactory;

public final class TopologicalIsometricSceneBufferFactory implements ISceneBufferFactory
{
	private final int m_tileWidth;
	private final int m_tileHeight;
	
	private final boolean m_debugDraw;
	
	public TopologicalIsometricSceneBufferFactory(int tileWidth, int tileHeight, boolean debugDraw)
	{
		m_tileWidth = tileWidth;
		m_tileHeight = tileHeight;
		m_debugDraw = debugDraw;
	}
	
	public TopologicalIsometricSceneBufferFactory(int tileWidth, int tileHeight)
	{
		this(tileWidth, tileHeight, false);
	}
	
	@Override
	public ISceneBuffer create()
	{
		return new TopologicalIsometricSceneBuffer(m_tileWidth, m_tileHeight, m_debugDraw);
	}
}
