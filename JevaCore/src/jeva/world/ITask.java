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
