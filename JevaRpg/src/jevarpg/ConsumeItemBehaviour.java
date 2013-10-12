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
