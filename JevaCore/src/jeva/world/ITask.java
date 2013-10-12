/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

/**
 * The Interface ITask.
 * 
 * @author Scott
 */
public interface ITask
{

	/**
	 * Cancel.
	 */
	void cancel();

	/**
	 * Begin.
	 * 
	 * @param entity
	 *            the entity
	 */
	void begin(Entity entity);

	/**
	 * End.
	 */
	void end();

	/**
	 * Do cycle.
	 * 
	 * @param deltaTime
	 *            the delta time
	 * @return true, if successful
	 */
	boolean doCycle(int deltaTime);

	/**
	 * Checks if is parallel.
	 * 
	 * @return true, if is parallel
	 */
	boolean isParallel();

	/**
	 * Ignores pause.
	 * 
	 * @return true, if successful
	 */
	boolean ignoresPause();
}
