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
package jeva;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Describes interface presented by core's resource library.
 */
public interface IResourceLibrary
{
	/**
	 * Opens an input stream to the specified resource.
	 * 
	 * @param path
	 *            The path of the resource to be accessed.
	 * @return An input stream to the specified resource.
	 */
	InputStream openResourceStream(String path);

	/**
	 * Returns the UTF-8 Decoded contents contain in the specified resources.
	 * 
	 * @param path
	 *            The path of the resource to be accessed.
	 * @return The UTF-Decoded contents of the specified resources.
	 * @Deprecated Routine too specific.
	 */
	String openResourceContents(String path);

	/**
	 * Open resource raw.
	 * 
	 * @param path
	 *            The path of the resource to be accessed.
	 * @return A byte buffer which contains the raw data of the specified
	 *         resource.
	 */
	ByteBuffer openResourceRaw(String path);

	/**
	 * Create a new state store at the specified path and constructs and
	 * OutputStream with which it can be accessed. If the state store already
	 * exists, it is overwritten.
	 * 
	 * @param path
	 *            The path of the resource to be accessed.
	 * @return An output stream to the specified resources.
	 * @throws StatelessEnvironmentException
	 *             Thrown if the application is running in a stateless
	 *             environment.
	 */
	OutputStream createState(String path) throws StatelessEnvironmentException;

	/**
	 * Opens an existing state store located at the specified path.
	 * 
	 * @param path
	 *            The path of the resource to be accessed.
	 * @return An input stream with which the resources can be read.
	 * @throws StatelessEnvironmentException
	 *             Thrown if the application is running in a stateless
	 *             environment.
	 */
	InputStream openState(String path) throws StatelessEnvironmentException;
}
