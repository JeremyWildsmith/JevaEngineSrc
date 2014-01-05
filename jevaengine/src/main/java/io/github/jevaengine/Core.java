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
package io.github.jevaengine;

import java.util.HashMap;
import java.util.Map;

import io.github.jevaengine.game.Game;
import io.github.jevaengine.ui.BasicWindowManager;
import io.github.jevaengine.ui.IWindowManager;

public final class Core
{
	private static HashMap<Class<?>, Object> m_existingServices;
	
	private static CoreMode m_coreMode = CoreMode.Normal;
	
	public enum CoreMode
	{
		LogicOnly(false),
		Normal(true);
		
		private boolean m_allowRender;
		
		CoreMode(boolean allowRender)
		{
			m_allowRender = allowRender;
		}
		
		public boolean allowsRender()
		{
			return m_allowRender;
		}
	}
	
	private Core()
	{
	}
	
	public static <T extends Game, Y extends ResourceLibrary, X extends IWindowManager> void initialize(T game, Y resourceLibrary, X windowManager, CoreMode coreMode)
	{
		if (m_existingServices != null)
			throw new CoreInitializationException("Core has already been initialized. Cannot repeat initialization");

		m_existingServices = new HashMap<Class<?>, Object>();

		m_existingServices.put(resourceLibrary.getClass(), resourceLibrary);
		m_existingServices.put(game.getClass(), game);
		m_existingServices.put(windowManager.getClass(), windowManager);
		
		m_coreMode = coreMode;
	}

	public static <T extends Game, Y extends ResourceLibrary> void initialize(T game, Y resourceLibrary)
	{
		initialize(game, resourceLibrary, new BasicWindowManager(), CoreMode.Normal);
	}
	
	public static <T extends Game, Y extends ResourceLibrary> void initialize(T game, Y resourceLibrary, CoreMode coreMode)
	{
		initialize(game, resourceLibrary, new BasicWindowManager(), coreMode);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getService(Class<T> typeClass)
	{
		if (m_existingServices == null)
			throw new CoreInitializationException("Core is being used without being initialized.");

		for (Map.Entry<Class<?>, Object> service : m_existingServices.entrySet())
		{
			if (typeClass.isAssignableFrom(service.getKey()))
				return (T) service.getValue();
		}

		throw new CoreInitializationException("Specified class has not been initialized");
	}

	public static CoreMode getMode()
	{
		return m_coreMode;
	}
	
	public static ICoreScriptBridge getScriptBridge()
	{
		return new ICoreScriptBridge()
		{
			@Override
			public void log(String debugMessage)
			{
				System.out.println("log: " + debugMessage);
			}
		};
	}

	public interface ICoreScriptBridge
	{
		void log(String debugMessage);
	}

}
