/*******************************************************************************
 * Copyright (c) 2013 Jeremy.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * If you'd like to obtain a another license to this code, you may contact Jeremy to discuss alternative redistribution options.
 * 
 * Contributors:
 *     Jeremy - initial API and implementation
 ******************************************************************************/
package io.github.jevaengine.world;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.Area;
import java.awt.image.VolatileImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.jevaengine.graphics.IRenderable;
import io.github.jevaengine.math.Matrix2X2;
import io.github.jevaengine.math.Vector2F;

public class SceneLighting
{

	private VolatileImage m_lightmap;

	private ArrayList<ILight> m_lightSources;

	private Rectangle m_renderTargetBounds;

	private Color m_ambientLight;

	public SceneLighting(int renderTargetWidth, int renderTargetHeight)
	{
		m_lightSources = new ArrayList();
		m_renderTargetBounds = new Rectangle(0, 0, renderTargetWidth, renderTargetHeight);

		m_ambientLight = new Color(0, 0, 0, 0);
	}

	public SceneLighting()
	{
		m_lightSources = new ArrayList();
		m_renderTargetBounds = new Rectangle(0, 0, 0, 0);
		m_ambientLight = new Color(0, 0, 0, 0);
	}

	public void setAmbientLight(Color color)
	{
		m_ambientLight = color;
	}

	public void addLight(ILight source)
	{
		m_lightSources.add(source);
	}

	public void removeLight(ILight source)
	{
		m_lightSources.remove(source);
	}

	public void setTargetBounds(int width, int height)
	{
		m_renderTargetBounds = new Rectangle(0, 0, width, height);
	}

	public int getTargetHeight()
	{
		return m_renderTargetBounds.getBounds().height;
	}

	public int getTargetWidth()
	{
		return m_renderTargetBounds.getBounds().width;
	}

	protected void enqueueRender(World world, GraphicsConfiguration gc, Rectangle viewBounds, final int offsetX, final int offsetY, float fScale)
	{

		if (m_renderTargetBounds.width <= 0 || m_renderTargetBounds.height <= 0)
			return;

		do
		{
			// Create new lightmap if it has not yet been initialized
			// or if its no longer valid\compatible
			// or if the viewbounds configuration has changed

			if (m_lightmap == null || m_lightmap.validate(gc) == VolatileImage.IMAGE_INCOMPATIBLE || (m_lightmap.getWidth(null) != m_renderTargetBounds.width && m_lightmap.getHeight(null) != m_renderTargetBounds.height))
			{
				m_lightmap = gc.createCompatibleVolatileImage(m_renderTargetBounds.width, m_renderTargetBounds.height, Transparency.TRANSLUCENT);
			}

			Graphics2D g = (Graphics2D) m_lightmap.getGraphics();

			g.setBackground(new Color(0, 0, 0, 0));
			g.clearRect(0, 0, m_renderTargetBounds.width, m_renderTargetBounds.height);

			HashMap<ILight, Area> lightAreaMap = new HashMap();

			for (ILight source : m_lightSources)
			{
				// Calculate area allocated by the light source.
				Area lightArea = source.getAllocation(world, viewBounds.x + offsetX, viewBounds.y + offsetY, fScale);

				lightAreaMap.put(source, lightArea);
			}

			for (Map.Entry<ILight, Area> light : lightAreaMap.entrySet())
			{
				light.getKey().renderComposite(world, g, light.getValue(), m_ambientLight, viewBounds.x + offsetX, viewBounds.y + offsetY, fScale);
			}

			Composite old = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0F));

			g.setColor(m_ambientLight);
			g.fillRect(0, 0, m_renderTargetBounds.width, m_renderTargetBounds.height);

			g.setComposite(old);

			for (Map.Entry<ILight, Area> light : lightAreaMap.entrySet())
			{
				light.getKey().renderLight(world, g, light.getValue(), m_ambientLight, viewBounds.x + offsetX, viewBounds.y + offsetY, fScale);
			}

			g.dispose();

		} while (m_lightmap.contentsLost());
		
		world.enqueueRender(new IRenderable()
		{
			@Override
			public void render(Graphics2D g, int x, int y, float fScale)
			{
				// If out contents are lost at this stage - the
				// operation is too expensive to repeat, so we just won't render the
				// image...
				if (!m_lightmap.contentsLost())
					g.drawImage(m_lightmap, offsetX, offsetY, null);
			}
		}, new Vector2F(Float.MAX_VALUE, Float.MAX_VALUE));
	}
}
