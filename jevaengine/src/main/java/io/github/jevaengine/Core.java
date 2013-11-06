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

	private Core()
	{
	}

	public static <T extends Game, Y extends IResourceLibrary, X extends IWindowManager> void initialize(T game, Y resourceLibrary, X windowManager)
	{
		if (m_existingServices != null)
			throw new CoreInitializationException("Core has already been initialized. Cannot repeat initialization");

		m_existingServices = new HashMap<Class<?>, Object>();

		m_existingServices.put(resourceLibrary.getClass(), resourceLibrary);
		m_existingServices.put(game.getClass(), game);
		m_existingServices.put(windowManager.getClass(), windowManager);
	}

	public static <T extends Game, Y extends IResourceLibrary> void initialize(T game, Y resourceLibrary)
	{
		initialize(game, resourceLibrary, new BasicWindowManager());
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

	public static ICoreScriptBridge getScriptBridge()
	{
		return new ICoreScriptBridge()
		{
			public void debugPrint(String debugMessage)
			{
				System.out.println("DebugPrint: " + debugMessage);
			}
		};
	}

	public interface ICoreScriptBridge
	{

		void debugPrint(String debugMessage);
	}

}
