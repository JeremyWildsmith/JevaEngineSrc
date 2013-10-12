package jeva.world;

/**
 * The Interface IWorldAssociation.
 */
public interface IWorldAssociation
{

	/**
	 * Checks if is associated.
	 * 
	 * @return true, if is associated
	 */
	boolean isAssociated();

	/**
	 * Disassociate.
	 */
	void disassociate();

	/**
	 * Associate.
	 * 
	 * @param world
	 *            the world
	 */
	void associate(World world);
}
