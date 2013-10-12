/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

/**
 * The Interface IInteractable.
 * 
 * @author Scott
 */

public interface IInteractable
{

	/**
	 * Gets the commands.
	 * 
	 * @return the commands
	 */
	public String[] getCommands();

	/**
	 * Do command.
	 * 
	 * @param bommand
	 *            the bommand
	 */
	void doCommand(String bommand);
}
