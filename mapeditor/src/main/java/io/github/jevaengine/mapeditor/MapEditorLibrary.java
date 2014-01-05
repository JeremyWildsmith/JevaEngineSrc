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

package io.github.jevaengine.mapeditor;

import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.ResourceLibraryIOException;
import io.github.jevaengine.Script;
import io.github.jevaengine.UnresolvedResourcePathException;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.JsonVariable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Jeremy
 */
public class MapEditorLibrary extends ResourceLibrary
{
	private File m_root;
	
	public MapEditorLibrary(String root)
	{
		m_root = new File(root);
	}
	
	@Override
	public IVariable openConfiguration(String path)
	{
		try
		{
			try (InputStream is = openAsset(path))
			{
				return JsonVariable.create(is);
			}
			
		} catch (FileNotFoundException ex)
		{
			throw new UnresolvedResourcePathException(path);
		} catch (IOException ex)
		{
			throw new ResourceLibraryIOException(ex, path);
		}
	}

	@Override
	public Script openScript(String path, Object context)
	{
		throw new UnsupportedOperationException("This resource library cannot create scripts");
	}

	@Override
	public InputStream openAsset(String path)
	{
		try
		{
			if(path.startsWith("@"))
			{
				InputStream is = this.getClass().getClassLoader().getResourceAsStream(path.substring(1));
			
				if (is == null)
					throw new UnresolvedResourcePathException(path);
				
				return is;
			}
			else
				return new FileInputStream(new File(m_root, path));
		} catch (FileNotFoundException ex)
		{
			throw new UnresolvedResourcePathException(path);
		}
	}
	
}
