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

package io.github.jevaengine.graphics.ui;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import io.github.jevaengine.game.ICamera;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.EventType;
import io.github.jevaengine.joystick.InputManager.InputMouseEvent.MouseButton;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.World;
import java.awt.Color;

public final class WorldView extends Panel
{
	private ICamera m_camera;

	private Listeners m_listeners = new Listeners();

	public WorldView(int width, int height)
	{
		super(width, height);
	}

	public void addListener(IWorldViewListener l)
	{
		m_listeners.add(l);
	}

	public void removeListener(IWorldViewListener l)
	{
		m_listeners.remove(l);
	}

	private Vector2D getCameraOffset()
	{
		if (m_camera == null)
			return new Vector2D();

		return new Vector2D(getBounds().width / 2, getBounds().height / 2).difference(m_camera.getLookAt());
	}

	public void setCamera(@Nullable ICamera camera)
	{
		m_camera = camera;
	}

	public void clear()
	{
		setCamera(null);
	}

	@Override
	public void onMouseEvent(InputMouseEvent mouseEvent)
	{
		if (mouseEvent.type == EventType.MouseClicked)
		{
			Vector2D relativePos = mouseEvent.location.difference(getAbsoluteLocation());

			World world = m_camera == null ? null : m_camera.getWorld();
			
			if (world != null)
			{
				Vector2D tilePos = world.translateScreenToWorld(new Vector2D(relativePos.x, relativePos.y).difference(getCameraOffset()), m_camera.getScale());

				if (world.getMapBounds().contains(new Point(tilePos.x, tilePos.y)))
					m_listeners.worldSelection(mouseEvent.location, tilePos, mouseEvent.mouseButton);
			}
			else
				super.onMouseEvent(mouseEvent);
		}
	}

	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		super.render(g, x, y, scale);

		World world = m_camera == null ? null : m_camera.getWorld();
		
		if (world != null)
		{
			Vector2D offset = getCameraOffset();
			Rectangle bounds = getBounds();

			Shape oldClip = g.getClip();

			g.clipRect(x, y, getBounds().width, getBounds().height);
			world.render(g, scale * m_camera.getScale(), new Rectangle(offset.x, offset.y, bounds.width, bounds.height), getAbsoluteLocation().x, getAbsoluteLocation().y);// bounds.width,																																		// bounds.height));
			g.setClip(oldClip);
			
			g.setColor(Color.black);
			g.drawRect(x, y, getBounds().width, getBounds().height);
		}
	}

	private static class Listeners extends StaticSet<IWorldViewListener>
	{
		public void worldSelection(Vector2D screenLocation, Vector2D worldLocation, MouseButton button)
		{
			for (IWorldViewListener l : this)
				l.worldSelection(screenLocation, worldLocation, button);
		}
	}

	public interface IWorldViewListener
	{
		void worldSelection(Vector2D screenLocation, Vector2D worldLocation, MouseButton button);
	}
}
