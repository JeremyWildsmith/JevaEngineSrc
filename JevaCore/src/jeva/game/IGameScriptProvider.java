package jeva.game;

import java.util.HashMap;

/**
 * The Interface IGameScriptProvider.
 */
public interface IGameScriptProvider
{
	/**
	 * Gets the game bridge.
	 * 
	 * @return the game bridge
	 */
	Object getGameBridge();

	/**
	 * Gets the globals.
	 * 
	 * @return the globals
	 */
	HashMap<String, Object> getGlobals();
}
