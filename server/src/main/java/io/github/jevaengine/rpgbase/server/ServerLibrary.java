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
package io.github.jevaengine.rpgbase.server;

import io.github.jevaengine.rpgbase.RpgCharacter;
import io.github.jevaengine.rpgbase.RpgLibrary;

public class ServerLibrary extends RpgLibrary
{
	public ServerLibrary()
	{
		registerEntity("character", RpgCharacter.class, new IEntityFactory<RpgCharacter>() {
			@Override
			public RpgCharacter create(IParentEntityFactory<RpgCharacter> parent, String instanceName, String config) {
				RpgCharacter c = parent.create(instanceName, config);
				
				//Wrap entity with server observer.
				new ServerRpgCharacter(c, instanceName, config);
				
				return c;
			}
		});
	}
}
