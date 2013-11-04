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
package io.github.jevaengine;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public interface IResourceLibrary
{

	InputStream openResourceStream(String path);

	String openResourceContents(String path);

	ByteBuffer openResourceRaw(String path);

	OutputStream createState(String path) throws StatelessEnvironmentException;

	InputStream openState(String path) throws StatelessEnvironmentException;
}
