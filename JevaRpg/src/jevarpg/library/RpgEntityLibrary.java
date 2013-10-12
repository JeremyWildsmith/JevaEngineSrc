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
package jevarpg.library;

import java.util.List;

import com.sun.istack.internal.Nullable;

import jeva.config.VariableValue;
import jeva.game.ResourceLoadingException;
import jeva.world.Entity;
import jeva.world.IEntityLibrary;
import jevarpg.AreaTrigger;
import jevarpg.RpgCharacter;

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
