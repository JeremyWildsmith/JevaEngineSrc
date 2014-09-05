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
package io.github.jevaengine.server.library;

import io.github.jevaengine.communication.SharedEntity;
import io.github.jevaengine.server.ServerEntity;
import io.github.jevaengine.util.Nullable;
import io.github.jevaengine.world.entity.DefaultEntity;

import java.util.List;

public interface IServerLibrary
{
	@Nullable
	<T extends DefaultEntity> IServerEntityWrapFactory getServerEntityWrapFactory(Class<T> entityClass);
	
	List<Class<? extends SharedEntity>> getSharedClasses();
	
	interface IServerEntityWrapFactory
	{
		ServerEntity<? extends DefaultEntity> wrap(DefaultEntity entity);
	}
}
