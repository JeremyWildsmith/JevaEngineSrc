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
package io.github.jevaengine.rpgbase;

import java.io.InputStream;
import java.util.Scanner;

import io.github.jevaengine.ResourceIOException;
import io.github.jevaengine.ResourceLibrary;
import io.github.jevaengine.Script;
import io.github.jevaengine.UnresolvedResourcePathException;
import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.config.JsonVariable;

import java.io.File;
import java.io.IOException;

public class RpgLibrary extends ResourceLibrary
{

	protected String m_base;
	protected boolean m_allowStateResource;

	public RpgLibrary()
	{
		m_base = "res";
		m_allowStateResource = true;
		
		registerEntity("character", RpgCharacter.class, new IEntityFactory<RpgCharacter>() {
			@Override
			public RpgCharacter create(IParentEntityFactory<RpgCharacter> parent, String instanceName, String config) {
				return new RpgCharacter(instanceName, openConfiguration(config));
			}
		});
		
		registerEntity("areaTrigger", AreaTrigger.class, new IEntityFactory<AreaTrigger>() {
			@Override
			public AreaTrigger create(IParentEntityFactory<AreaTrigger> parent, String instanceName, String config) {
				return new AreaTrigger(instanceName, openConfiguration(config));
			}
		});
	}

	public RpgLibrary(boolean allowStates)
	{
		m_base = "res";
		m_allowStateResource = allowStates;
	}

	public String openResourceContents(String path)
	{
		InputStream srcStream = openAsset(path);

		Scanner scanner = new Scanner(srcStream, "UTF-8");
		scanner.useDelimiter("\\A");

		String contents = (scanner.hasNext() ? scanner.next() : "");

		scanner.close();

		return contents;
	}
	
	public String resolvePath(String path)
	{
		return new File(new File(m_base), path).getPath().replace("\\", "/");
	}

	@Override
	public InputStream openAsset(String path)
	{
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(resolvePath(path));

		if (is == null)
			throw new UnresolvedResourcePathException(path);

		return is;
	}

	@Override
	public IVariable openConfiguration(String path)
	{
		try
		{
			return JsonVariable.create(openAsset(path));
		} catch (IOException ex)
		{
			throw new ResourceIOException(ex, path);
		}
	}

	@Override
	public Script openScript(String path, Object context)
	{
		Script script = new Script(context);
		
		script.evaluate(openResourceContents(path));
		
		return script;
	}
}
