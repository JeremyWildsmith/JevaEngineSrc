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
package jeva;

import java.util.HashMap;
import java.util.Map;

import proguard.annotation.KeepClassMemberNames;
import jeva.game.Game;

/**
 * The Core module provides access to the engine's services and provides the
 * engine's core script bridge. Such services include the main resource library.
 * The Core must be initialized before the engine is used.
 */
public final class Core
{
	/** A map of registered services to their respective instance. */
	private static HashMap<Class<?>, Object> m_existingServices;

	/**
	 * Internal constructor, not exposed by utility class.
	 */
	private Core()
	{
	}

	/**
	 * Initializes core so that it can be used by the engine. Provides engine
	 * with a game instance to work with and a resource library.
	 * 
	 * @param <T>
	 *            The type of the Game instance to be used
	 * @param <Y>
	 *            The type of the Resource Library instance to be used
	 * @param game
	 *            A reference to the main game to be used by the core.
	 * @param resourceLibrary
	 *            A reference to the resource library to be used byu the core.
	 */
	public static <T extends Game, Y extends IResourceLibrary> void initializeCore(T game, Y resourceLibrary)
	{
		if (m_existingServices != null)
			throw new CoreInitializationException("Core has already been initialized. Cannot repeat initialization");

		m_existingServices = new HashMap<Class<?>, Object>();

		m_existingServices.put(resourceLibrary.getClass(), resourceLibrary);
		m_existingServices.put(game.getClass(), game);
	}

	/**
	 * Gets the instances corresponding to the specified service type.
	 * 
	 * @param <T>
	 *            The type of the service to be acquired.
	 * @param typeClass
	 *            The respective Class to the given type.
	 * @return An instance of the corresponding service.
	 */
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

	/**
	 * Gets the core's script bridge.
	 * 
	 * @return Returns the core's script bridge.
	 */
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

	/**
	 * The interface provided by the core script bridge.
	 */
	@KeepClassMemberNames
	public interface ICoreScriptBridge
	{
		/**
		 * Prints a debug message to the configured output stream.
		 * 
		 * @param debugMessage
		 *            The message to be written to the debug output stream.
		 */
		void debugPrint(String debugMessage);
	}

}
