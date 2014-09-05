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

package io.github.jevaengine.ui;

import io.github.jevaengine.game.ICamera;
import io.github.jevaengine.game.NullCamera;
import io.github.jevaengine.graphics.IImmutableGraphic;
import io.github.jevaengine.graphics.NullGraphic;
import io.github.jevaengine.joystick.InputKeyEvent;
import io.github.jevaengine.joystick.InputMouseEvent;
import io.github.jevaengine.math.Rect2D;
import io.github.jevaengine.math.Vector2D;
import io.github.jevaengine.math.Vector2F;
import io.github.jevaengine.math.Vector3F;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.util.StaticSet;
import io.github.jevaengine.world.IImmutableSceneBuffer;
import io.github.jevaengine.world.entity.IEntity;
import io.github.jevaengine.world.scene.NullSceneBuffer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;

public final class WorldView extends Control
{
	public static final String COMPONENT_NAME = "worldView";
	
	private final int m_desiredWidth;
	private final int m_desiredHeight;
	
	private IImmutableGraphic m_frame;
	
	private Observers m_observers = new Observers();
	
	private ICamera m_camera = new NullCamera();
	
	private IImmutableSceneBuffer m_lastScene = new NullSceneBuffer();
	
	public WorldView(int desiredWidth, int desiredHeight)
	{
		super(COMPONENT_NAME);
		m_desiredWidth = desiredWidth;
		m_desiredHeight = desiredHeight;
		m_frame = new NullGraphic(desiredWidth, desiredHeight);
	}
	
	public WorldView(String instanceName, int desiredWidth, int desiredHeight)
	{
		super(COMPONENT_NAME, instanceName);
		m_desiredWidth = desiredWidth;
		m_desiredHeight = desiredHeight;
		m_frame = new NullGraphic(desiredWidth, desiredHeight);
	}

	public void addObserver(IWorldViewObserver o)
	{
		m_observers.add(o);
	}

	public void removeObserver(IWorldViewObserver o)
	{
		m_observers.remove(o);
	}
	
	@Override
	public Rect2D getBounds()
	{
		return m_frame.getBounds();
	}

	public void setCamera(ICamera camera)
	{
		m_camera = camera;
	}

	public Vector2F translateScreenToWorld(Vector2F relativeLocation)
	{
		if(m_camera == null)
			return new Vector2F();
		
		return m_lastScene.translateScreenToWorld(new Vector3F(relativeLocation, m_camera.getLookAt().z), 1.0F);
	}

	@Nullable
	public <T extends IEntity> T pick(Class<T> clazz, Vector2D location)
	{
		if (m_lastScene != null)
		{
			return m_lastScene.pick(clazz, location.x, location.y, 1.0F);
		}else
			return null;
	}
	
	@Override
	public boolean onMouseEvent(InputMouseEvent mouseEvent)
	{
		InputMouseEvent relativeMouseEvent = new InputMouseEvent(mouseEvent);
		relativeMouseEvent.location = mouseEvent.location.difference(getAbsoluteLocation());
			
		m_observers.mouseEvent(relativeMouseEvent);
			
		return true;
	}

	@Override
	public boolean onKeyEvent(InputKeyEvent keyEvent){ return false; }

	@Override
	public void update(int deltaTime) { }
	
	@Override
	public void render(Graphics2D g, int x, int y, float scale)
	{
		m_frame.render(g, x, y, scale);
		
		g.setColor(Color.black);
		g.fillRect(x, y, getBounds().width, getBounds().height);
			
		Shape oldClip = g.getClip();
		g.clipRect(x, y, getBounds().width, getBounds().height);
		
		m_lastScene = m_camera.getScene(getBounds(), scale);
		m_lastScene.render(g, x, y, scale);
			
		g.setClip(oldClip);
	}

	@Override
	public void onStyleChanged()
	{
		m_frame = getComponentStyle().getStateStyle(ComponentState.Default).createFrame(m_desiredWidth, m_desiredHeight);
	}
	
	private static class Observers extends StaticSet<IWorldViewObserver>
	{
		public void mouseEvent(InputMouseEvent event)
		{
			for (IWorldViewObserver l : this)
				l.mouseEvent(event);
		}
	}

	public interface IWorldViewObserver
	{
		void mouseEvent(InputMouseEvent event);
	}
}
