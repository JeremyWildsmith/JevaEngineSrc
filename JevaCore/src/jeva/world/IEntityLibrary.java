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
