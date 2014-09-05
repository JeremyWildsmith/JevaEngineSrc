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

package io.github.jevaengine.worldbuilder;

import io.github.jevaengine.IAssetStreamFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 *
 * @author Jeremy
 */
public final class BuilderAssetStreamFactory implements IAssetStreamFactory
{
	private final File m_assetSource;
	
	public BuilderAssetStreamFactory(String assetSource)
	{
		m_assetSource = new File(assetSource);
	}
	
	private File resolvePath(String relativePath)
	{
		File file = new File(m_assetSource, relativePath);
			
		if(file.exists())
			return file;
		
		return new File(relativePath);
	}
	
	@Override
	public InputStream create(String path) throws AssetStreamConstructionException
	{
		try
		{
			if(path.startsWith("@"))
			{
				InputStream is = this.getClass().getClassLoader().getResourceAsStream(path.substring(1));
			
				if (is == null)
					throw new AssetStreamConstructionException(path, new UnresolvedResourcePathException());
				
				return is;
			}
			else
			{
				File resolvedPath = new File(path);
				
				if(!resolvedPath.isAbsolute())
					resolvedPath = resolvePath(path);

				return new FileInputStream(resolvedPath);
			}
		} catch (FileNotFoundException ex)
		{
			throw new AssetStreamConstructionException(path, new UnresolvedResourcePathException());
		}
	}
	
	public final class UnresolvedResourcePathException extends Exception
	{
		private static final long serialVersionUID = 1L;

		private UnresolvedResourcePathException() { }
	}
	
	public final class NoRootAssignedException extends RuntimeException
	{
		private static final long serialVersionUID = 1L;

		private NoRootAssignedException()
		{
			super("No root directory has been assigned!");
		}
	}
}
