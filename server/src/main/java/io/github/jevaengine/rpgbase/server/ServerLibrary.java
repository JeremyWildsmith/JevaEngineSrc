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
package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.rpgbase.RpgLibrary;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;

public class ServerLibrary extends RpgLibrary
{
	@Override
	public Entity createEntity(String entityName, @Nullable String instanceName, String config)
	{
		// Override RpgCharacter implementation with networked RPG Character
		if (entityName.compareTo("character") == 0)
			return new ServerRpgCharacter(instanceName, instanceName, config).getControlledEntity();
		else
			return super.createEntity(entityName, instanceName, config);
	}
}
