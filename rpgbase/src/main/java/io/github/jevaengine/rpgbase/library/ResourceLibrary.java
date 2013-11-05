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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.jevaengine.rpgbase.library;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import io.github.jevaengine.IResourceLibrary;
import io.github.jevaengine.ResourceLibraryIOException;
import io.github.jevaengine.StatelessEnvironmentException;
import io.github.jevaengine.UnresolvedResourcePathException;

public class ResourceLibrary implements IResourceLibrary
{

	protected String m_base;
	protected boolean m_allowStateResource;

	public ResourceLibrary()
	{
		m_base = "res";
		m_allowStateResource = true;
	}

	public ResourceLibrary(boolean allowStates)
	{
		m_base = "res";
		m_allowStateResource = allowStates;
	}

	public String resolvePath(String path)
	{
		return new File(new File(m_base), path).getPath().replace("\\", "/");
	}

	@Override
	public String openResourceContents(String path)
	{
		InputStream srcStream = openResourceStream(path);

		Scanner scanner = new Scanner(srcStream, "UTF-8");
		scanner.useDelimiter("\\A");

		String bontents = (scanner.hasNext() ? scanner.next() : "");

		scanner.close();

		return bontents;
	}

	public ByteBuffer openResourceRaw(String path)
	{
		InputStream srcStream = openResourceStream(path);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try
		{

			byte[] readBuffer = new byte[2048];

			int length = 0;

			while ((length = srcStream.read(readBuffer, 0, readBuffer.length)) != -1)
			{
				bos.write(readBuffer, 0, length);
			}
		} catch (UnsupportedEncodingException e)
		{
			throw new ResourceLibraryIOException("Unsupported stream encoding. " + e.getMessage());
		} catch (IOException e)
		{
			throw new ResourceLibraryIOException("Error occured while attempting to read file: " + e.getMessage());
		}

		return ByteBuffer.wrap(bos.toByteArray());
	}

	public InputStream openResourceStream(String path)
	{
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(resolvePath(path));

		if (is == null)
			throw new UnresolvedResourcePathException(path);

		return is;
	}

	@Override
	public InputStream openState(String path) throws StatelessEnvironmentException
	{
		if (!m_allowStateResource)
			throw new StatelessEnvironmentException();

		try
		{
			return new FileInputStream(new File(resolvePath(path)));
		} catch (FileNotFoundException e)
		{
			throw new UnresolvedResourcePathException(path);
		}
	}

	@Override
	public OutputStream createState(String path) throws StatelessEnvironmentException
	{
		File file = new File(resolvePath(path));

		if (!m_allowStateResource)
			throw new StatelessEnvironmentException();

		try
		{
			if (!file.exists())
				file.createNewFile();

			return new FileOutputStream(file);
		} catch (IOException e)
		{
			throw new ResourceLibraryIOException("Error occured while attempting to create file: " + e.getMessage());
		}
	}
}
