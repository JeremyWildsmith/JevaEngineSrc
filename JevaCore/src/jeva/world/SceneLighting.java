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
package jeva.world;

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

import jeva.graphics.IRenderable;
import jeva.math.Matrix2X2;
import jeva.math.Vector2F;

/**
 * The Class SceneLighting.
 */
public class SceneLighting implements IWorldAssociation
{

	/** The m_lightmap. */
	private VolatileImage m_lightmap;

	/** The m_light sources. */
	private ArrayList<ILight> m_lightSources;

	/** The m_render target bounds. */
	private Rectangle m_renderTargetBounds;

	/** The m_parent world. */
	private World m_parentWorld;

	/** The m_ambient light. */
	private Color m_ambientLight;

	/**
	 * Instantiates a new scene lighting.
	 * 
	 * @param renderTargetWidth
	 *            the render target width
	 * @param renderTargetHeight
	 *            the render target height
	 */
	public SceneLighting(int renderTargetWidth, int renderTargetHeight)
	{
		m_lightSources = new ArrayList<ILight>();
		m_renderTargetBounds = new Rectangle(0, 0, renderTargetWidth, renderTargetHeight);

		m_ambientLight = new Color(0, 0, 0, 0);
	}

	/**
	 * Instantiates a new scene lighting.
	 */
	public SceneLighting()
	{
		m_lightSources = new ArrayList<ILight>();
		m_renderTargetBounds = new Rectangle(0, 0, 0, 0);
		m_ambientLight = new Color(0, 0, 0, 0);
	}

	/**
	 * Sets the ambient light.
	 * 
	 * @param color
	 *            the new ambient light
	 */
	public void setAmbientLight(Color color)
	{
		m_ambientLight = color;
	}

	/**
	 * Adds the light.
	 * 
	 * @param source
	 *            the source
	 */
	public void addLight(ILight source)
	{
		m_lightSources.add(source);
	}

	/**
	 * Removes the light.
	 * 
	 * @param source
	 *            the source
	 */
	public void removeLight(ILight source)
	{
		m_lightSources.remove(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.IWorldAssociation#isAssociated()
	 */
	@Override
	public final boolean isAssociated()
	{
		return m_parentWorld != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.IWorldAssociation#associate(jeva.world.World)
	 */
	@Override
	public final void associate(World world)
	{
		if (m_parentWorld != null)
			throw new WorldAssociationException("Already associated with world");

		m_parentWorld = world;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.IWorldAssociation#disassociate()
	 */
	@Override
	public final void disassociate()
	{
		if (m_parentWorld == null)
			throw new WorldAssociationException("Not associated with world");

		m_parentWorld = null;
	}

	/**
	 * Gets the world.
	 * 
	 * @return the world
	 */
	private World getWorld()
	{
		if (m_parentWorld == null)
			throw new WorldAssociationException("Not associated with world");

		return m_parentWorld;
	}

	/**
	 * Sets the target bounds.
	 * 
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 */
	public void setTargetBounds(int width, int height)
	{
		m_renderTargetBounds = new Rectangle(0, 0, width, height);
	}

	/**
	 * Gets the target height.
	 * 
	 * @return the target height
	 */
	public int getTargetHeight()
	{
		return m_renderTargetBounds.getBounds().height;
	}

	/**
	 * Gets the target width.
	 * 
	 * @return the target width
	 */
	public int getTargetWidth()
	{
		return m_renderTargetBounds.getBounds().width;
	}

	/**
	 * Enqueue render.
	 * 
	 * @param gc
	 *            the gc
	 * @param worldViewBounds
	 *            the world view bounds
	 * @param worldTranslationMatrix
	 *            the world translation matrix
	 * @param offsetX
	 *            the offset x
	 * @param offsetY
	 *            the offset y
	 * @param fScale
	 *            the f scale
	 */
	protected void enqueueRender(GraphicsConfiguration gc, Rectangle worldViewBounds, Matrix2X2 worldTranslationMatrix, int offsetX, int offsetY, float fScale)
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

			HashMap<ILight, Area> lightAreaMap = new HashMap<ILight, Area>();
			HashMap<ILight, ArrayList<LightObstruction>> obstructionMap = new HashMap<ILight, ArrayList<LightObstruction>>();

			for (ILight source : m_lightSources)
			{
				// Calculate area allocated by the light source.
				Area lightArea = source.getAllocation(fScale, offsetX, offsetY);

				lightAreaMap.put(source, lightArea);

				/*
				 * Subtract from it collision area, first from our map
				 * TileEffects[] encompassedTileEffects =
				 * getWorld().getTileEffectMap().getTileEffects(new
				 * TransformShapeSearchFilter<TileEffects>(
				 * worldTranslationMatrix.scale(fScale), lightArea));
				 * 
				 * obstructionMap.put(source, new
				 * ArrayList<LightObstruction>());
				 * 
				 * Area obstructionArea = new Area();
				 * 
				 * for(TileEffects tileEffects : encompassedTileEffects) {
				 * Vector2D obstructionOrigin =
				 * getWorld().translateWorldToScreen(new
				 * Vector2F(tileEffects.location), fScale);//.difference(new
				 * Vector2D(-iOffsetX, -iOffsetY));
				 * 
				 * Vector2F difference = new
				 * Vector2F(tileEffects.location).difference
				 * (source.getLocation());
				 * 
				 * if(tileEffects.sightEffect <= 0.8F &&
				 * difference.getLengthSquared() > 0.5F) { Vector2D
				 * shadowProjection =
				 * getWorld().translateWorldToScreen(difference, fScale);
				 * 
				 * Vector2F obstructionShadeNormal = new
				 * Vector2F(-shadowProjection.y,
				 * shadowProjection.x).normalize(); Vector2F obstructionShadeTop
				 * =
				 * obstructionShadeNormal.multiply(-getWorld().getTileHeight()/
				 * 2); Vector2F obstructionShadeBottom =
				 * obstructionShadeNormal.multiply
				 * (+getWorld().getTileHeight()/2);
				 * 
				 * Polygon obstructionPolygon = new Polygon();
				 * obstructionPolygon.addPoint(obstructionOrigin.x +
				 * (int)(obstructionShadeTop.x), obstructionOrigin.y +
				 * (int)(obstructionShadeTop.y));
				 * obstructionPolygon.addPoint(obstructionOrigin.x +
				 * (int)(obstructionShadeBottom.x), obstructionOrigin.y +
				 * (int)(obstructionShadeBottom.y));
				 * 
				 * obstructionPolygon.addPoint(obstructionOrigin.x +
				 * shadowProjection.x * 10 + (int)(obstructionShadeBottom.x),
				 * obstructionOrigin.y + shadowProjection.y * 10 +
				 * (int)(obstructionShadeBottom.y));
				 * obstructionPolygon.addPoint(obstructionOrigin.x +
				 * shadowProjection.x * 10 + (int)(obstructionShadeTop.x),
				 * obstructionOrigin.y + shadowProjection.y * 10 +
				 * (int)(obstructionShadeTop.y));
				 * 
				 * Area obstruction = new Area(obstructionPolygon);
				 * obstruction.intersect(lightArea);
				 * obstruction.subtract(obstructionArea);
				 * 
				 * obstructionMap.get(source).add(new
				 * LightObstruction(obstruction, new Vector2F(shadowProjection),
				 * tileEffects.sightEffect)); obstructionArea.add(obstruction);
				 * } }
				 */
			}

			for (Map.Entry<ILight, Area> light : lightAreaMap.entrySet())
			{
				light.getKey().renderComposite(g, light.getValue(), m_ambientLight, obstructionMap.get(light.getKey()), offsetX, offsetY, fScale);
			}

			Composite old = g.getComposite();
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, 1.0F));

			g.setColor(m_ambientLight);
			g.fillRect(0, 0, m_renderTargetBounds.width, m_renderTargetBounds.height);

			g.setComposite(old);

			for (Map.Entry<ILight, Area> light : lightAreaMap.entrySet())
			{
				light.getKey().renderLight(g, light.getValue(), m_ambientLight, obstructionMap.get(light.getKey()), offsetX, offsetY, fScale);
			}

			g.dispose();

		} while (m_lightmap.contentsLost());

		getWorld().enqueueRender(new IRenderable()
		{
			@Override
			public void render(Graphics2D g, int x, int y, float fScale)
			{
				// If out contents are lost at this stage - the
				// operation is too
				// expensive to repeat, so we just won't render the
				// image...
				if (!m_lightmap.contentsLost())
					g.drawImage(m_lightmap, 0, 0, null);
			}
		}, new Vector2F(worldViewBounds.x + worldViewBounds.width, worldViewBounds.y + worldViewBounds.height));
	}
}
