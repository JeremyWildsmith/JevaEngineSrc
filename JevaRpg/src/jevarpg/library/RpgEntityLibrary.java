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
