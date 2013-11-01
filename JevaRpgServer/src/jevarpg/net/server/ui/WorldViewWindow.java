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

package jevarpg.net.server.ui;

import java.awt.event.KeyEvent;

import jeva.Core;
import jeva.game.ControlledCamera;
import jeva.game.Game;
import jeva.graphics.ui.Button;
import jeva.graphics.ui.IWindowManager;
import jeva.graphics.ui.MenuStrip;
import jeva.graphics.ui.MenuStrip.IMenuStripListener;
import jeva.graphics.ui.Window;
import jeva.graphics.ui.WorldView;
import jeva.graphics.ui.WorldView.IWorldViewListener;
import jeva.joystick.InputManager.InputKeyEvent;
import jeva.joystick.InputManager.InputKeyEvent.EventType;
import jeva.joystick.InputManager.InputMouseEvent.MouseButton;
import jeva.math.Vector2D;
import jeva.math.Vector2F;
import jeva.world.IInteractable;
import jeva.world.World;

public class WorldViewWindow extends Window
{
	private ControlledCamera m_camera = new ControlledCamera();
	private Vector2F m_cameraMovement = new Vector2F();
	
	private MenuStrip m_contextStrip = new MenuStrip();
	
	public WorldViewWindow(World world)
	{
		super(Core.getService(Game.class).getGameStyle(), 400, 400);
		
		m_camera.attach(world);
		
		WorldView worldView = new WorldView(360, 370);
		worldView.setRenderBackground(false);
		worldView.setCamera(m_camera);
		worldView.addListener(new WorldViewListener());
		
		this.addControl(worldView, new Vector2D(15, 15));
		
		this.addControl(new Button("Close") {
			
			@Override
			public void onButtonPress()
			{
				Core.getService(IWindowManager.class).removeWindow(WorldViewWindow.this);
			}
		}, new Vector2D(2,2));
		
		this.addControl(m_contextStrip);
	}
	
	@Override
	public void update(int delta)
	{
		super.update(delta);
		
		if (!m_cameraMovement.isZero())
			m_camera.move(m_cameraMovement.normalize().multiply(0.3F));
	}
	
	@Override
	public void onKeyEvent(InputKeyEvent e)
	{
		if(e.isConsumed)
			return;
		
		if(e.type == EventType.KeyUp)
			m_cameraMovement = new Vector2F();
		else
		{
			switch (e.keyCode)
			{
			case KeyEvent.VK_UP:
				e.isConsumed = true;
				m_cameraMovement.y = -1;
				break;
			case KeyEvent.VK_RIGHT:
				e.isConsumed = true;
				m_cameraMovement.x = 1;
				break;
			case KeyEvent.VK_DOWN:
				e.isConsumed = true;
				m_cameraMovement.y = 1;
				break;
			case KeyEvent.VK_LEFT:
				e.isConsumed = true;
				m_cameraMovement.x = -1;
				break;
			}
		}
	}
	
	private class WorldViewListener implements IWorldViewListener
	{

		@Override
		public void worldSelection(Vector2D screenLocation, Vector2D worldLocation, MouseButton button)
		{
			final IInteractable[] interactables = m_camera.getWorld().getTileEffects(worldLocation).interactables.toArray(new IInteractable[0]);

	        if (button == MouseButton.Right)
	        {
	            if (interactables.length > 0 && interactables[0].getCommands().length > 0)
	            {
	            	m_contextStrip.setContext(interactables[0].getCommands(), new IMenuStripListener()
	                {
	                    @Override
	                    public void onCommand(String command)
	                    {
	                        interactables[0].doCommand(command);
	                    }
	                });
	            	
	            	m_contextStrip.setLocation(screenLocation.difference(WorldViewWindow.this.getAbsoluteLocation()));
	            }
	        }

		}
		
	}
}
