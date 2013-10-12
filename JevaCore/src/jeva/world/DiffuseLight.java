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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;
import java.util.List;

import jeva.math.Matrix2X2;
import jeva.math.Vector2D;
import jeva.math.Vector2F;

/**
 * The Class DiffuseLight.
 */
public abstract class DiffuseLight implements ILight
{

	/** The m_color. */
	private Color m_color;

	/** The m_light cast. */
	private Area m_lightCast;

	/** The m_world transform. */
	private AffineTransform m_worldTransform;

	/** The m_world. */
	private World m_world;

	/** The m_f radius. */
	private float m_fRadius;

	/**
	 * Instantiates a new diffuse light.
	 * 
	 * @param world
	 *            the world
	 * @param fMaxRadius
	 *            the f max radius
	 * @param color
	 *            the color
	 */
	public DiffuseLight(World world, float fMaxRadius, Color color)
	{
		m_color = color;

		m_world = world;
		m_fRadius = fMaxRadius;
		m_lightCast = new Area(new Ellipse2D.Float(-fMaxRadius / 2, -fMaxRadius / 2, fMaxRadius, fMaxRadius));

		Matrix2X2 worldScreenMatrix = world.getPerspectiveMatrix(1.0F);
		m_worldTransform = new AffineTransform(worldScreenMatrix.matrix[0][0], worldScreenMatrix.matrix[0][1], worldScreenMatrix.matrix[1][0], worldScreenMatrix.matrix[1][1], 0, 0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ILight#renderComposite(java.awt.Graphics2D,
	 * java.awt.geom.Area, java.awt.Color, java.util.List, int, int, float)
	 */
	public void renderComposite(Graphics2D g, Area lightingArea, Color ambient, List<LightObstruction> obstructedLightingArea, int x, int y, float fScale)
	{
		g.translate(x, y);
		g.transform(m_worldTransform);

		AffineTransform reverse = new AffineTransform();

		try
		{
			reverse.concatenate(m_worldTransform.createInverse());
			reverse.translate(-x, -y);
		} catch (NoninvertibleTransformException e)
		{
			throw new RuntimeException(e);
		}

		g.setPaint(new RadialGradientPaint(getLocation().x, getLocation().y, m_fRadius, new float[]
		{ 0, 0.5F }, new Color[]
		{ Color.black, new Color(0, 0, 0, 0) }));
		g.fill(lightingArea.createTransformedArea(reverse));

		try
		{
			g.transform(m_worldTransform.createInverse());
			g.translate(-x, -y);
		} catch (NoninvertibleTransformException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ILight#renderLight(java.awt.Graphics2D,
	 * java.awt.geom.Area, java.awt.Color, java.util.List, int, int, float)
	 */
	public void renderLight(Graphics2D g, Area lightingArea, Color ambient, List<LightObstruction> obstructedLightingArea, int x, int y, float fScale)
	{
		g.translate(x, y);
		g.transform(m_worldTransform);

		AffineTransform reverse = new AffineTransform();

		try
		{
			reverse.concatenate(m_worldTransform.createInverse());
			reverse.translate(-x, -y);
		} catch (NoninvertibleTransformException e)
		{
			throw new RuntimeException(e);
		}

		g.setPaint(new RadialGradientPaint(getLocation().x, getLocation().y, m_fRadius, new float[]
		{ 0, 0.5F }, new Color[]
		{ m_color, new Color(0, 0, 0, 0) }));
		g.fill(lightingArea.createTransformedArea(reverse));

		try
		{
			g.transform(m_worldTransform.createInverse());
			g.translate(-x, -y);
		} catch (NoninvertibleTransformException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ILight#getAllocation(float, int, int)
	 */
	public Area getAllocation(float fWorldScale, int offsetX, int offsetY)
	{
		Vector2D location = m_world.translateWorldToScreen(getLocation(), fWorldScale);// .add(new
																						// Vector2D(offsetX,
																						// offsetY));

		Area worldShape = m_lightCast.createTransformedArea(m_worldTransform);

		AffineTransform transform = new AffineTransform();
		transform.translate(location.x + offsetX, location.y + offsetY);
		transform.scale(fWorldScale, fWorldScale);
		worldShape.transform(transform);

		return worldShape;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jeva.world.ILight#getLocation()
	 */
	public abstract Vector2F getLocation();
}
