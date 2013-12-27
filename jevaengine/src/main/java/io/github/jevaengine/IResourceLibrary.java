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

import io.github.jevaengine.config.IVariable;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.Entity;
import java.io.InputStream;

public interface IResourceLibrary
{
	IVariable openConfiguration(String path);
	Script openScript(String path, Object context);
	Entity createEntity(String entityName, @Nullable String instanceName, String config);
	InputStream openAsset(String path);
	//IRenderable openTexture(String path);
}
