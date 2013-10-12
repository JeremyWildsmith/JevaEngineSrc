package jeva.world;

import java.awt.Rectangle;

import jeva.math.Vector2F;

import com.sun.istack.internal.Nullable;

/**
 * The Interface ISearchFilter.
 * 
 * @param <T>
 *            the generic type
 */
public interface ISearchFilter<T>
{

	/**
	 * Gets the search bounds.
	 * 
	 * @return the search bounds
	 */
	public abstract Rectangle getSearchBounds();

	/**
	 * Should include.
	 * 
	 * @param location
	 *            the location
	 * @return true, if successful
	 */
	public abstract boolean shouldInclude(Vector2F location);

	/**
	 * Filter.
	 * 
	 * @param item
	 *            the item
	 * @return the t
	 */
	@Nullable
	public abstract T filter(T item);
}
