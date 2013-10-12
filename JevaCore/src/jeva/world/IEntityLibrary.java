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
package jeva.world;

import java.util.List;

import jeva.config.VariableValue;

import com.sun.istack.internal.Nullable;

/**
 * The Interface IEntityLibrary.
 */
public interface IEntityLibrary
{

	/**
	 * Creates the entity.
	 * 
	 * @param entityName
	 *            the entity name
	 * @param instanceName
	 *            the instance name
	 * @param arguments
	 *            the arguments
	 * @return the entity
	 */
	Entity createEntity(String entityName, @Nullable String instanceName, List<VariableValue> arguments);
}
