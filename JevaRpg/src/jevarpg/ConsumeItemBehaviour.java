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
package jevarpg;

import javax.script.ScriptException;

import jeva.CoreScriptException;

public class ConsumeItemBehaviour implements IItemBehaviour
{

    @Override
    public boolean use(Item item, RpgCharacter user, RpgCharacter target)
    {
        try
        {
            Object o = item.getScript().invokeScriptFunction("use", user.getScriptBridge());

            if (!(o instanceof Boolean))
                throw new CoreScriptException("onConsume routine did not return proper boolean to indicate removal.");

            return ((Boolean) o).booleanValue();
        } catch (ScriptException e)
        {
            throw new CoreScriptException("Error occured while executing item onConsume routine: " + e.getMessage());
        } catch (NoSuchMethodException e)
        {
            return false;
        }
    }

}
