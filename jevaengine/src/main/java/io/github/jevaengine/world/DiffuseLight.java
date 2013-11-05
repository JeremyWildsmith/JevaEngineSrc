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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.NoninvertibleTransformException;

import io.github.jevaengine.math.Matrix2X2;
import io.github.jevaengine.math.Vector2D;

public abstract class DiffuseLight implements ILight
{

	private Color m_color;

	private Area m_lightCast;

	private float m_fRadius;

	public DiffuseLight(float fMaxRadius, Color color)
	{
		m_color = color;

		m_fRadius = fMaxRadius;
		m_lightCast = new Area(new Ellipse2D.Float(-fMaxRadius / 2, -fMaxRadius / 2, fMaxRadius, fMaxRadius));
	}
	
	private AffineTransform getWorldTransform(World world, float scale)
	{
		Matrix2X2 worldScreenMatrix = world.getPerspectiveMatrix(scale);
		return new AffineTransform(worldScreenMatrix.matrix[0][0], worldScreenMatrix.matrix[0][1], worldScreenMatrix.matrix[1][0], worldScreenMatrix.matrix[1][1], 0, 0);
	}
	
	@Override
	public final void setColor(Color color)
	{
		m_color = color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ILight#renderComposite(java.awt.Graphics2D,
	 * java.awt.geom.Area, java.awt.Color, java.util.List, int, int, float)
	 */
	@Override
	public final void renderComposite(World world, Graphics2D g, Area lightingArea, Color ambient, int x, int y, float scale)
	{
		g.setPaint(new RadialGradientPaint(getLocation().x, getLocation().y, m_fRadius, new float[]
												{ 0, 0.5F }, new Color[]
												{ Color.black, new Color(0, 0, 0, 0) }));
		g.fill(lightingArea);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ILight#renderLight(java.awt.Graphics2D,
	 * java.awt.geom.Area, java.awt.Color, java.util.List, int, int, float)
	 */
	@Override
	public final void renderLight(World world, Graphics2D g, Area lightingArea, Color ambient, int x, int y, float scale)
	{
		AffineTransform worldTransform = getWorldTransform(world, scale);
		
		g.translate(x, y);
		g.transform(worldTransform);

		AffineTransform reverse = new AffineTransform();

		try
		{
			reverse.concatenate(worldTransform.createInverse());
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
			g.transform(worldTransform.createInverse());
			g.translate(-x, -y);
		} catch (NoninvertibleTransformException e)
		{
			throw new RuntimeException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.github.jeremywildsmith.jevaengine.world.ILight#getAllocation(float, int, int)
	 */
	@Override
	public final Area getAllocation(World world, int offsetX, int offsetY, float scale)
	{
		Vector2D location = world.translateWorldToScreen(getLocation(), scale);

		Area worldShape = m_lightCast.createTransformedArea(getWorldTransform(world, scale));

		AffineTransform transform = new AffineTransform();
		transform.translate(location.x + offsetX, location.y + offsetY);
		transform.scale(scale, scale);
		worldShape.transform(transform);

		return worldShape;
	}
}
