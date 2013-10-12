package jevarpg;

import javax.script.ScriptException;

import jeva.CoreScriptException;

public class AttackItemBehaviour implements IItemBehaviour
{
    @Override
    public boolean use(Item item, RpgCharacter user, RpgCharacter target)
    {
        try
        {
            Object o = item.getScript().invokeScriptFunction("use", user.getScriptBridge(), target.getScriptBridge());

            if (!(o instanceof Boolean))
                throw new CoreScriptException("onAttack routine did not return proper boolean to indicate removal.");

            return ((Boolean) o).booleanValue();
        } catch (ScriptException e)
        {
            throw new CoreScriptException("Error occured while executing item onAttack routine: " + e.getMessage());
        } catch (NoSuchMethodException e)
        {
            return false;
        }
    }

}
