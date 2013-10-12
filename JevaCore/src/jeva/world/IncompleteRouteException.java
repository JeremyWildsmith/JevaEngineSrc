/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jeva.world;

/**
 * The Class IncompleteRouteException.
 * 
 * @author Scott
 */
public class IncompleteRouteException extends Exception
{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The m_incomplete route. */
	private RouteNode[] m_incompleteRoute;

	/**
	 * Instantiates a new incomplete route exception.
	 * 
	 * @param incompleteRoute
	 *            the incomplete route
	 */
	public IncompleteRouteException(RouteNode[] incompleteRoute)
	{
		m_incompleteRoute = incompleteRoute;
	}

	/**
	 * Gets the incomplete route.
	 * 
	 * @return the incomplete route
	 */
	public RouteNode[] getIncompleteRoute()
	{
		return m_incompleteRoute;
	}
}
