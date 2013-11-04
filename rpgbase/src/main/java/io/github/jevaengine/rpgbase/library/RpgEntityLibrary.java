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
package io.github.jevaengine.rpgbase.library;

import java.util.List;

import io.github.jevaengine.config.VariableValue;
import io.github.jevaengine.game.ResourceLoadingException;
import io.github.jevaengine.rpgbase.AreaTrigger;
import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import io.github.jevaengine.world.IEntityLibrary;

public class RpgEntityLibrary implements IEntityLibrary
{
	@Override
	public Entity createEntity(String entityName, @Nullable String instanceName, List<VariableValue> arguments)
	{
		if (entityName.compareTo("jevarpg.RpgCharacter") == 0)
			return new RpgCharacter(instanceName, arguments);
		else if (entityName.compareTo("jevarpg.AreaTrigger") == 0)
			return new AreaTrigger(instanceName, arguments);
		else
			throw new ResourceLoadingException("Error constructing entity for: " + entityName + " not found in asset library");
	}

}
