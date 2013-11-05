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
import java.awt.geom.Area;

import io.github.jevaengine.math.Vector2F;

public interface ILight
{

	Area getAllocation(World world, int offsetX, int offsetY, float fScale);
	void renderLight(World world, Graphics2D g, Area lightingArea, Color ambient, int x, int y, float fScale);
	
	void renderComposite(World world, Graphics2D g, Area lightingArea, Color ambient, int x, int y, float fScale);
	Vector2F getLocation();
	
	public void setColor(Color color);
}
